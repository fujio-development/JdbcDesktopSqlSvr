package desk.xml;

import java.io.Serializable;

/** Microsoft SQLServerと接続のための設定値格納
 * @author none **/
public class SqlSvrInfo extends Config implements Serializable {
	private String _MssqlSvHost;
	private String _MssqlSvInstance;
	private int _MssqlSvPort;
	private String _MssqlSvCatalog;
	private boolean _MssqlSvLoginMode;
	private String _MssqlSvUserID;
	private String _MssqlSvPassword;
	private int _MssqlSvConnectTimeout;
	private boolean _MssqlSvMars;
	
	private static final String DefaultFileName = "MsSqlServer";
	private static final long serialVersionUID = 1L;
	
	public SqlSvrInfo() {
		this._ConfigFileName = DefaultFileName;
	}
	
	/** ホスト名(サーバー名)
	 * @return String **/
	public String getMssqlSvHost() {
		return this._MssqlSvHost;
	}
	/** ホスト名(サーバー名)
	 * @param value **/
	public void setMssqlSvHost(String value) {
		this._MssqlSvHost = value;
	}
	
	/** インスタンス名
	 * @return String **/
	public String getMssqlSvInstance() {
		return this._MssqlSvInstance;
	}
	/** インスタンス名
	 * @param value **/
	public void setMssqlSvInstance(String value) {
		this._MssqlSvInstance = value;
	}
	
	/** ポート(ローカルホストの場合は自動的に無効とします)
	 * @return int **/
	public int getMssqlSvPort() {
		return this._MssqlSvPort;
	}
	/** ポート(ローカルホストの場合は自動的に無効とします)
	 * @param value **/
	public void setMssqlSvPort(int value) {
		this._MssqlSvPort = value;
	}
	
	/** データベース名
	 * @return String **/
	public String getMssqlSvCatalog() {
		return this._MssqlSvCatalog;
	}
	/** データベース名
	 * @param value **/
	public void setMssqlSvCatalog(String value) {
		this._MssqlSvCatalog = value;
	}
	
	/** 認証モード(Windows統合認証=true、SQLServer認証=false)
	 * @return String **/
	public boolean getMssqlSvLoginMode() {
		return this._MssqlSvLoginMode ;
	}
	/** 認証モード(Windows統合認証=true、SQLServer認証=false)
	 * @param value **/
	public void setMssqlSvLoginMode(boolean value) {
		this._MssqlSvLoginMode = value;
	}
	/** SQLServer ユーザーID
	 * @return String **/
	public String getMssqlSvUserID() {
		return this._MssqlSvUserID;
	}
	/** SQLServer ユーザーID
	 * @param value **/
	public void setMssqlSvUserID(String value) {
		this._MssqlSvUserID = value;
	}
	
	/** SQLServer パスワード
	 * @return String **/
	public String getMssqlSvPassword() {
		return this._MssqlSvPassword;
	}
	/** SQLServer パスワード
	 * @param value **/
	public void setMssqlSvPassword(String value) {
		this._MssqlSvPassword = value;
	}
	
	/** オープン時の接続タイムアウト(秒単位)
	 * @return int **/
	public int getMssqlSvConnectTimeout() {
		return this._MssqlSvConnectTimeout;
	}
	/** オープン時の接続タイムアウト(秒単位)
	 * @param value **/
	public void setMssqlSvConnectTimeout(int value) {
		this._MssqlSvConnectTimeout = value;
	}
	
	/**MultipleActiveResultSetsを使用するか
	 * @return boolean **/
	public boolean getMssqlSvMars() {
		return this._MssqlSvMars;
	}
	/** MultipleActiveResultSetsを使用するか
	 * @param value **/
	public void setMssqlSvMars(boolean value) {
		this._MssqlSvMars = value;
	}
}