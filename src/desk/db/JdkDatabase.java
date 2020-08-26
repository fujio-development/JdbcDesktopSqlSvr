package desk.db;

import java.util.List;
import java.util.HashMap;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import desk.LastException;

/** JDBC基底クラス
 * @author none **/
public abstract class JdkDatabase extends LastException {
	protected String _Driver;
	protected String _UrlHead;
	protected String _Host;
	protected String _Instance;
	protected int _Port;
	protected String _Catalog;
	protected String _UserID;
	protected String _Password;
	
	//抽象メソッド
	protected abstract void ConecString();
	public abstract void Open() throws ClassNotFoundException, SQLException;
	public abstract void Close() throws SQLException;
	public abstract List<HashMap<String, String>> ExecuteQuery(String SelectSql) throws SQLException;
	public abstract <DTO> List<DTO> ExecuteQuery(Class<DTO> type, String SelectSql) throws SQLException, IllegalAccessException,
	InstantiationException, InvocationTargetException, NoSuchMethodException;
	public abstract void clearParameters(String LoSql) throws SQLException;
	public abstract void setParameters(Object value) throws SQLException;
	public abstract int ExecuteUpdate() throws SQLException;
	
	/** DBドライバー名
	 * @return String **/
	public String getDriver() {
		return this._Driver;
	}
	/** DBドライバー名
	 * @param value **/
	public void setDriver(String value) {
		this._Driver = value;
	}

	/** Url接続文字列先頭の固定文字
	 * @return String **/
	public String getUrlHead() {
		return this._UrlHead;
	}
	/** Url接続文字列先頭の固定文字
	 * @param value **/
	public void setUrlHead(String value) {
		this._UrlHead = value;
	}

	/** ホスト名(サーバー名)またはIPアドレス<br />
	 *  サーバー／クライアント型のみ必要
	 * @return String **/
	public String getHost() {
		return this._Host;
	}
	/** ホスト名(サーバー名)またはIPアドレス<br />
	 *  サーバー／クライアント型のみ必要
	 * @param value **/
	public void setHost(String value) {
		this._Host = value;
	}

	/** インスタンス名<br />
	 *  サーバー／クライアント型のみ必要
	 * @return String **/
	public String getInstance() {
		return this._Instance;
	}
	/** インスタンス名<br />
	 *  サーバー／クライアント型のみ必要
	 * @param value **/
	public void setInstance(String value) {
		this._Instance = value;
	}

	/** ネットワーク ポート番号<br />
	 *  サーバー／クライアント型のみ必要
	 * @return int **/
	public int getPort() {
		return this._Port;
	}
	/** ネットワーク ポート番号<br />
	 *  サーバー／クライアント型のみ必要
	 * @param value **/
	public void setPort(int value) {
		this._Port = value;
	}

	/** データベース名
	 * @return String **/
	public String getCatalog() {
		return this._Catalog;
	}
	/** データベース名
	 * @param value **/
	public void setCatalog(String value) {
		this._Catalog = value;
	}

	/** ログインユーザー名<br />
	 *  OS統合認証の場合は必要ありません
	 * @return String **/
	public String getUserID() {
		return this._UserID;
	}
	/** ログインユーザー名<br />
	 *  OS統合認証の場合は必要ありません
	 * @param value **/
	public void setUserID(String value) {
		this._UserID = value;
	}

	/** ログインパスワード<br />
	 *  パスワード認証が無い場合は必要ありません
	 * @return String **/
	public String getPassword() {
		return this._Password;
	}
	/** ログインパスワード<br />
	 *  パスワード認証が無い場合は必要ありません
	 * @param value **/
	public void setPassword(String value) {
		this._Password = value;
	}
}