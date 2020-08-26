package desk.xml;

/** 設定ファイルでアプリケーションをサポートする基底クラス
 * @author none **/
public abstract class Config {
	protected String _ConfigFileName;
	
	public String getConfigFileName() {
		return this._ConfigFileName;
	}
	public void setConfigFileName(String value) {
		this._ConfigFileName = value;
	}
}