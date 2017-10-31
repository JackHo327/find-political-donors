
/*
 * @author: Renzhi He
 * @date: 2017/10/31
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindDonors {

	public static void main(String[] args) {

		//./input/itcont.txt ./output/medianvals_by_zip.txt ./output/medianvals_by_date.txt
		if (args.length == 3) {
			// generate the output of "medianvals_by_zip.txt"
			genOutput("." + args[0], "." + args[1], "zip");

			// generate the output of "medianvals_by_date.txt"
			genOutput("." + args[0], "." + args[2], "date");
		}

		// // generate the output of "medianvals_by_zip.txt"
		// genOutput("../input/itcont.txt", "../output/medianvals_by_zip.txt", "zip");

		// // generate the output of "medianvals_by_date.txt"
		// genOutput("../input/itcont.txt", "../output/medianvals_by_date.txt", "date");

	}

	//testing output
	// CMTE_ID: 0: C00629618
	// AMNDT_IND: 1: N
	// RPT_TP: 2: TER
	// TRANSACTION_PGI: 3: P
	// IMAGE_NUM: 4: 201701230300133512
	// TRANSACTION_TP: 5: 15C
	// ENTITY_TP: 6: IND
	// NAME: 7: PEREZ, JOHN A
	// CITY: 8: LOS ANGELES
	// STATE: 9: CA
	// ZIP_CODE: 10: 90017
	// EMPLOYER: 11: PRINCIPAL
	// OCCUPATION: 12: DOUBLE NICKEL ADVISORS
	// TRANSACTION_DT: 13: 01032017
	// TRANSACTION_AMT: 14: 40
	// OTHER_ID: 15: H6CA34245
	// TRAN_ID: 16: SA01251735122
	// FILE_NUM: 17: 1141239
	// MEMO_CD: 18:
	// MEMO_TEXT: 19:
	// SUB_ID: 20: 2012520171368850783

	public static void genOutput(String fileLoc, String newFileLoc, String type) {

		ArrayList<Record> recList = new ArrayList<>();
		Map<String, ArrayList<Record>> mp = new HashMap<>();
		Map<String, String> opt = new HashMap<>();

		try {

			BufferedReader bfr = new BufferedReader(
					new InputStreamReader(new FileInputStream(new File(fileLoc)), "UTF-8"));

			BufferedWriter bwr = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(new File(newFileLoc)), "UTF-8"));
			String line = "";

			while ((line = bfr.readLine()) != null) {

				// for every record, it should contains 21 fields
				// I will drop the one do not have 21 fields
				String[] records = line.trim().split("\\|");
				if (records.length != 21) {
					continue; // drop this one and continue loop -- reading
							// records
				} else {

					// only keep the record whose OTHER_ID is empty
					if (!records[15].equals("")) {
						continue;
					}

					// check malformat when calculate valur by date
					if (type.toLowerCase().trim().equals("date")) {
						// drop malformat date fields
						if (!checkDate(records[13].trim())) {
							continue;
						}
					}

					// check the malformat zip
					if (type.toLowerCase().equals("zip")) {

						if (!checkZip(records[10].trim())) {
							continue;
						} else {
							// keep the 1st five digit
							records[10] = records[10].trim().substring(0, 5);
						}
					}

					// check empty CMTE_ID and TRANSACTION_AMT
					if (records[0].equals("") || records[14].equals("")) {
						continue;
					}

					// String[] subStrs = new String[4];
					// subStrs[0] = records[0].trim(); // CMTE_ID
					// subStrs[1] = records[10].trim().substring(0, 5); //
					// ZIP_CODE
					// subStrs[2] = records[13].trim(); // TRANSACTION_DT
					// subStrs[3] = records[14].trim(); // TRANSACTION_AMT
					// subStrs[4] = "1"; // 1 freq
					// subStrs[5] = records[15].trim(); // OTHER_ID
					// System.out.println(Arrays.toString(subStrs));

					Record rec = new Record(records[0].trim(), records[10].trim().substring(0, 5),
							records[13].trim(), Long.parseLong(records[14].trim()));

					if (type.equals("zip")) {
						// mock a fake streaming processing
						Map<String, String> m = calculateByZip(rec, mp, opt);
						String[] strs = m.get(rec.getCmteId()).split("\\|");
						bwr.write(strs[0] + "|" + rec.getZipCode() + "|" + strs[1] + "|" + strs[3] + "|"
								+ strs[2] + "\n");
						m.remove(rec.getZipCode());
						bwr.flush();

					} else if (type.equals("date")) {
						// can be a batch processing here
						recList.add(new Record(records[0].trim(), records[10].trim().substring(0, 5),
								records[13].trim(), Long.parseLong(records[14].trim())));

					}

				}
			}

			bfr.close();

			if (type.equals("date") && recList.size() > 0) {
				// do batch process here
				// call calculateByDate()

				// for conveniention, just nake this function return ArrayList
				// and the subcontent is <ID| Date| median| Freq| sum>
				ArrayList<String> mm = calculateByDate(recList, mp, opt);

				Collections.sort(mm);

				for (String k : mm) {
					// System.out.println(k);
					bwr.write(k);
					bwr.newLine();
					bwr.flush();
				}
			}

			bwr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean checkDate(String dateStr) {
		boolean bol = true;

		if (dateStr.isEmpty() || dateStr == null) {
			bol = false;
		} else {
			Date date = null;

			SimpleDateFormat simDF = new SimpleDateFormat("mmddyyyy");
			simDF.setLenient(false);
			try {
				date = simDF.parse(dateStr);
				// System.out.println("date: " + date);
				if (!dateStr.equals(simDF.format(date))) {
					bol = false;
				}

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return bol;
	}

	public static boolean checkZip(String zipCode) {
		boolean bol = true;
		Pattern p = Pattern.compile("^[0-9]{5,9}$");
		Matcher m = p.matcher(zipCode);

		if (!m.matches()) {
			bol = false;
		}

		return bol;
	}

	public static Map<String, String> calculateByZip(Record rec, Map<String, ArrayList<Record>> zipOthers,
			Map<String, String> ret) {

		if (zipOthers.containsKey(rec.getZipCode())) {

			// new the Record ArrayList, corresponding to the zip code
			zipOthers.get(rec.getZipCode()).add(rec);

			// sort that arrayList based on the transAmt - Ascending
			zipOthers.get(rec.getZipCode()).sort(Comparator.comparing(a -> a.getTransAmt()));

			int length = zipOthers.get(rec.getZipCode()).size();

			long median = 0;

			if (length % 2 == 0) {
				// if there are "even" number of records
				int n1 = length / 2;
				int n2 = n1 - 1;

				median = Math.round(zipOthers.get(rec.getZipCode()).get(n2).getTransAmt()
						+ (zipOthers.get(rec.getZipCode()).get(n1).getTransAmt()
								- zipOthers.get(rec.getZipCode()).get(n2).getTransAmt()) / 2.0);
			} else {
				// if there are "odd" number of records
				int n1 = (int) length / 2;
				median = zipOthers.get(rec.getZipCode()).get((n1 - 1) / 2).getTransAmt();

			}

			long sum = 0;
			for (Record recc : zipOthers.get(rec.getZipCode())) {
				sum += recc.getTransAmt();
			}

			StringBuilder sb = new StringBuilder();

			// <zipcode, id|median|sum|length>
			ret.put(rec.getCmteId(), sb.append(rec.getCmteId()).append("|").append(median).append("|").append(sum)
					.append("|").append(length).toString());

		} else {

			zipOthers.put(rec.getZipCode(), new ArrayList<Record>());
			zipOthers.get(rec.getZipCode()).add(rec);
			StringBuilder sb = new StringBuilder();

			ret.put(rec.getCmteId(), sb.append(rec.getCmteId()).append("|").append(rec.getTransAmt()).append("|")
					.append(rec.getTransAmt()).append("|").append(1).toString());

		}
		return ret;
	}

	public static ArrayList<String> calculateByDate(ArrayList<Record> records,
			Map<String, ArrayList<Record>> dateOthers, Map<String, String> ret) {

		ArrayList<String> returnStr = new ArrayList<>();

		for (Record rec : records) {
			if (dateOthers.containsKey(rec.getTransDt())) {

				// new the Record ArrayList, corresponding to the zip code
				dateOthers.get(rec.getTransDt()).add(rec);

				// sort that arrayList based on the transAmt - Ascending
				dateOthers.get(rec.getTransDt()).sort(Comparator.comparing(a -> a.getTransAmt()));

				int length = dateOthers.get(rec.getTransDt()).size();

				long median = 0;

				if (length % 2 == 0) {
					// if there are "even" number of records
					int n1 = length / 2;
					int n2 = n1 - 1;

					median = Math.round(dateOthers.get(rec.getTransDt()).get(n2).getTransAmt()
							+ (dateOthers.get(rec.getTransDt()).get(n1).getTransAmt()
									- dateOthers.get(rec.getTransDt()).get(n2).getTransAmt()) / 2.0);
				} else {
					// if there are "odd" number of records
					int n1 = (int) length / 2;
					median = dateOthers.get(rec.getTransDt()).get((n1 - 1) / 2).getTransAmt();

				}

				long sum = 0;
				for (Record recc : dateOthers.get(rec.getTransDt())) {
					sum += recc.getTransAmt();
				}

				StringBuilder sb = new StringBuilder();

				// <zipcode, id|median|sum|length>
				ret.put(rec.getCmteId(), sb.append(rec.getTransDt()).append("|").append(median).append("|")
						.append(sum).append("|").append(length).toString());

			} else {

				dateOthers.put(rec.getTransDt(), new ArrayList<Record>());
				dateOthers.get(rec.getTransDt()).add(rec);
				StringBuilder sb = new StringBuilder();

				ret.put(rec.getCmteId(), sb.append(rec.getTransDt()).append("|").append(rec.getTransAmt())
						.append("|").append(rec.getTransAmt()).append("|").append(1).toString());

			}
		}

		for (String k : ret.keySet()) {
			String[] strs = ret.get(k).split("\\|");
			returnStr.add(k + "|" + strs[0] + "|" + strs[1] + "|" + strs[3] + "|" + strs[2]);
		}

		return returnStr;
	}

}
