/*
 * @author: Renzhi He
 * @date: 2017/10/31
 */

public class Record {

	private String cmteId;
	private String zipCode;
	private String transDt;
	private long transAmt;

	public Record(String cmteId, String zipCode, String transDt, long transAmt) {
		super();
		this.cmteId = cmteId;
		this.zipCode = zipCode;
		this.transDt = transDt;
		this.transAmt = transAmt;
	}

	public String getCmteId() {
		return cmteId;
	}

	public void setCmteId(String cmteId) {
		this.cmteId = cmteId;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getTransDt() {
		return transDt;
	}

	public void setTransDt(String transDt) {
		this.transDt = transDt;
	}

	public long getTransAmt() {
		return transAmt;
	}

	public void setTransAmt(long transAmt) {
		this.transAmt = transAmt;
	}

	@Override
	public String toString() {
		return "Record [cmteId=" + cmteId + ", zipCode=" + zipCode + ", transDt=" + transDt + ", transAmt="
				+ transAmt + "]";
	}

}
