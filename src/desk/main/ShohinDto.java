package desk.main;

import java.math.BigDecimal;

/** Data Transfer Object(DTO)クラス
 * @author none **/
public class ShohinDto {
	
	private Integer _NumId;
	private Short _ShohinNum;
	private String _ShohinName;
	private BigDecimal _EditDate;
	private BigDecimal _EditTime;
	private String _Note;
	
	public Integer getNumId() {
		return this._NumId;
	}
	public void setNumId(Integer value) {
		this._NumId = value;
	}
	
	public Short getShohinNum() {
		return this._ShohinNum;
	}
	public void setShohinNum(Short value) {
		this._ShohinNum = value;
	}
	
	public String getShohinName() {
		return this._ShohinName;
	}
	public void setShohinName(String value) {
		this._ShohinName = value;
	}
	
	public BigDecimal getEditDate() {
		return this._EditDate;
	}
	public void setEditDate(BigDecimal value) {
		this._EditDate = value;
	}
	
	public BigDecimal getEditTime() {
		return this._EditTime;
	}
	public void setEditTime(BigDecimal value) {
		this._EditTime = value;
	}
	
	public String getNote() {
		return this._Note;
	}
	public void setNote(String value) {
		this._Note = value;
	}
}