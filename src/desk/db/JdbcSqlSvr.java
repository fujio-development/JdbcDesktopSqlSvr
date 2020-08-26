package desk.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.microsoft.sqlserver.jdbc.StringUtils;

import desk.LastException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/** Microsoft JDBC Driver xx for SQL Server<br />
 *  DAO(Data Access Object)
 * @author none **/
public class JdbcSqlSvr extends JdkDatabase implements AutoCloseable {
	protected Connection _Conec;
	private PreparedStatement PrPrestm;
	private ResultSet PrResultSet = null;
	protected boolean _OpenFlag = false;
	protected boolean _LoginMode;
	protected int _ConnectTimeout;
	protected boolean _MultipleActiveResultSets;
	private String ConecStr;
	private int paraCnt;
	
	public JdbcSqlSvr() {
	}
	
	/** ConnectionString設定
	 **/
	@Override
	protected void ConecString() {		
		String[] LoHostStr = {"(local)", "localhost", "127.0.0.1", "."};
		boolean LoHostCheck = false;
		String Wserver = "";
		
		//接続先がローカルかチェック
		if (Arrays.asList(LoHostStr).contains(_Host))
			LoHostCheck = true;			
		
		//接続先の代入
		if (LoHostCheck == true) { //ローカルなら
			if (StringUtils.isEmpty(_Instance)) { //nullまたは空文字なら
				//既定のインスタンス
				Wserver = _Host;
			} else {
				//名前付きのインスタンス
				Wserver = _Host + "\\" + _Instance;
			}
		} else {
			if (StringUtils.isEmpty(_Instance)) {
				//既定のインスタンス、固定ポート
				Wserver = _Host + "," + _Port;
			} else {
				//名前付きインスタンス、固定ポート
				Wserver = _Host + "\\" + _Instance + "," + _Port;
			}
		}
		
		//**接続文字列作成**
		_Driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		_UrlHead = "jdbc:sqlserver://";
		ConecStr = _UrlHead + Wserver + ";databaseName=" + _Catalog + ";integratedSecurity=" + _LoginMode;
		ConecStr += ";loginTimeout=" + _ConnectTimeout + ";multipleactiveresultsets=" + _MultipleActiveResultSets;
	}

	protected void GoOpen() throws ClassNotFoundException, SQLException {
		ConecString();		
		try {
			Class.forName(_Driver); //JDBC4.0以上では不要ですが差し支えないと思われる
			_Conec = DriverManager.getConnection(ConecStr, _UserID, _Password);
			_Conec.setAutoCommit(false);
		} catch(ClassNotFoundException | SQLException ex) {
			String cmethod = Thread.currentThread().getStackTrace()[1].getMethodName();
			LastException.SetLastException(cmethod, "", ex);
			LastException.LogWrite();
			throw ex;
		}
		_OpenFlag = true;
	}
	
	/** データベースに接続します(外部設定ファイルを使わずに接続)<br />
	 *  ※setter値を基に接続します。setterに直接代入してから使用して下さい **/
	@Override
	public void Open() throws ClassNotFoundException, SQLException {
		if (_OpenFlag == false) 
			GoOpen();
	}

	private void GoClose() throws SQLException {
		String cmethod = new Object(){}.getClass().getEnclosingMethod().getName();
		
		try {
			if (PrResultSet != null) {
				PrResultSet.close();
				PrResultSet = null;
			}
		} catch (SQLException sqlex) {
			LastException.SetLastException(cmethod, "", sqlex);
			LastException.LogWrite();
			throw sqlex;
		} finally {
			try {
				if (PrPrestm != null) {
					PrPrestm.close(); //Statementを閉じるとResultSetも自動で閉じる。API仕様
					PrPrestm = null;
				}
			} catch (SQLException sqlex) {
				LastException.SetLastException(cmethod, "", sqlex);
				LastException.LogWrite();
				throw sqlex;
			} finally {
				try {
					if (_Conec != null && !_Conec.isClosed()) {
						_Conec.close();
						_Conec = null;
					}
				} catch (SQLException sqlex) {
					LastException.SetLastException(cmethod, "", sqlex);
					LastException.LogWrite();
					throw sqlex;
				}
			}
		}
		_OpenFlag = false;
	}
	
	/** 接続しているデータベースを閉じます<br />
	 *  ※AutoClosable実装メソッド **/
	@Override
	public void close() throws SQLException {
		GoClose();
	}
	
	/** 接続しているデータベースを閉じます
	 *  呼び出し側からの直接破棄 **/
	@Override
	public void Close() throws SQLException {		
		GoClose();
	}

	/** 与えられたSELECT文でPreparedStatementのexecuteQueryメソッドを実行し<br />
	 *  List< HashMap< 列名, 値 > >で返します<br />
	 * @param SelectSql SELECT文
	 * @return List< HashMap< String, String > >**/
	@Override
	public List<HashMap<String, String>> ExecuteQuery(String SelectSql) throws SQLException {
		HashMap<String, String> Lohsmap = null;
		List<HashMap<String, String>> Lolist = new ArrayList<>();
		ResultSetMetaData Lomdata = null;

		try (PreparedStatement Lopsmt = _Conec.prepareStatement(SelectSql); ResultSet Lorset = Lopsmt.executeQuery();) {
			Lomdata = Lorset.getMetaData();
			Lolist.clear();
			while(Lorset.next()) {
				Lohsmap.clear();
				Lohsmap = new HashMap<>();
				for (short i = 1 ;i < Lomdata.getColumnCount();i++) {
					Lohsmap.put(Lomdata.getColumnName(i), Lorset.getString(i));
				}
				Lolist.add(Lohsmap);
			}
		} catch (SQLException sqlex) {
			String cmethod = new Object(){}.getClass().getEnclosingMethod().getName();
			LastException.SetLastException(cmethod, "", sqlex);
			LastException.LogWrite();
			throw sqlex;
		}

		return Lolist;
	}

	/** 与えられたSELECT文でPreparedStatementのExecuteReaderメソッドを実行しIEnumerable< T型 >で返します
	* @param type ジェネリクスT型
	* @param SelectSql SELECT文
	* @return List< DTO > **/
	@Override
	public <DTO> List<DTO> ExecuteQuery(Class<DTO> type, String SelectSql) throws SQLException, IllegalAccessException,
	InstantiationException, InvocationTargetException, NoSuchMethodException {
		List<DTO> Lolist = new ArrayList<DTO>();
		
		try (PreparedStatement Lopsmt = _Conec.prepareStatement(SelectSql); ResultSet Lorset = Lopsmt.executeQuery();) {
			Lolist.clear();
			while (Lorset.next()) {
				DTO t = type.getDeclaredConstructor().newInstance();
				for (Field field : t.getClass().getDeclaredFields()) {
					field.setAccessible(true);
					String fname = field.getName().replace("_", ""); 
					String value = Lorset.getString(fname);
					field.set(t, field.getType().getConstructor(String.class).newInstance(value));
				}
				Lolist.add(t);
			}
		} catch (SQLException | IllegalAccessException | InstantiationException | 
				InvocationTargetException | NoSuchMethodException ex) {
			String cmethod = new Object(){}.getClass().getEnclosingMethod().getName();
			LastException.SetLastException(cmethod, "", ex);
			LastException.LogWrite();
			throw ex;
		}
		
		return Lolist;
	}

	/** 与えられたSELECT文でPreparedStatementクラスのexecuteQuery()メソッドを実行しResultSetで返します
	* @param SelectSql SELECT文
	* @return ResultSet **/
	public ResultSet ExecuteQueryDirect(String SelectSql) throws SQLException {
		PreparedStatement SelectPsmt = null;
		
		try {
			SelectPsmt = _Conec.prepareStatement(SelectSql);
			PrResultSet = SelectPsmt.executeQuery();
		} catch (SQLException sqlex) {
			String cmethod = new Object(){}.getClass().getEnclosingMethod().getName();
			LastException.SetLastException(cmethod, "", sqlex);
			LastException.LogWrite();
			throw sqlex;
		}
		
		return PrResultSet;
	}

	/** ResultSetを閉じます
	 **/
	public void ResultSetClose() throws SQLException {
		if (PrResultSet != null) {
			try {
				PrResultSet.close();
			} catch (SQLException sqlex){
				String cmethod = new Object(){}.getClass().getEnclosingMethod().getName();
				LastException.SetLastException(cmethod, "", sqlex);
				LastException.LogWrite();
				throw sqlex;
			}
		}
	}
	
	/** SELECT COUNT文でPreparedStatementのexecuteQeryメソッドを実行し件数を返します
	 * @param LoSql select count(*) cnt
	 * @return int 全件数
	 * @throws SQLException **/
	public int ExecuteCount(String LoSql) throws SQLException {
		int cnt = 0;
		
		try (PreparedStatement Lopstm = _Conec.prepareStatement(LoSql); ResultSet Lorset = Lopstm.executeQuery();) {
			if (Lorset.next()) {
				cnt = Lorset.getInt("cnt");
			}
		} catch (SQLException sqlex) {
			String cmethod = new Object(){}.getClass().getEnclosingMethod().getName();
			LastException.SetLastException(cmethod, "", sqlex);
			LastException.LogWrite();
			throw sqlex;
		}
		
		return cnt;
	}
	
	/** クラス内のPreparedStatementを使用しパラメータオブジェクトの作成とパラメータ値クリアー
	 * @param LoSql INSERT,UPDATE,DELETE,CREATE,ALTER,DROP文
	 * @throws SQLException **/
	@Override
	public void clearParameters(String LoSql) throws SQLException {
		try {
			PrPrestm = _Conec.prepareStatement(LoSql);
			PrPrestm.clearParameters();
		} catch (SQLException sqlex) {
			String cmethod = new Object(){}.getClass().getEnclosingMethod().getName();
			LastException.SetLastException(cmethod, "", sqlex);
			LastException.LogWrite();
			throw sqlex;
		}
	}
	
	/** クラス内のPreparedStatementによるパラメータを代入します。
	 * @param value 値　**/
	@Override
	public void setParameters(Object value) throws SQLException {
		paraCnt += 1;
		try {
			PrPrestm.setObject(paraCnt, value);
		} catch (SQLException sqlex) {
			String cmethod = new Object(){}.getClass().getEnclosingMethod().getName();
			LastException.SetLastException(cmethod, "", sqlex);
			LastException.LogWrite();
			throw sqlex;
		}
	}
	
	/** クラス内のPreparedStatementによるexecuteUpdate()メソッドを実行します<br />
	* このメソッドを実行する前にclearParametersメソッドの初期化とsetParametersメソッドで値をセットして下さい
	* @return int SQL文で影響を受けた件数 
	* @throws SQLException **/
	@Override
	public int ExecuteUpdate() throws SQLException {
		int cnt = 0;
		
		try {
			//PrPrestm = _Conec.prepareStatement(LoSql);
			try {
				PrPrestm.executeUpdate();
			} catch (SQLException sqlex) {
				String cmethod = new Object(){}.getClass().getEnclosingMethod().getName();
				LastException.SetLastException(cmethod, "", sqlex);
				LastException.LogWrite();
				_Conec.rollback();
				throw sqlex;
			}
			_Conec.commit();
			PrPrestm.close();
		} catch (SQLException ex) {
			String cmethod = new Object(){}.getClass().getEnclosingMethod().getName();
			LastException.SetLastException(cmethod, "", ex);
			LastException.LogWrite();
			throw ex;
		}
		
		return cnt;
	}
	
	/** 呼び出し側からのPreparedStatementオブジェクトによるexecuteUpdate()メソッドを実行します
	 * @param LoPrestm 呼び出し側からの引数によるプリペアドステートメント
	 * @return int SQL文で影響を受けた件数 
	 * @throws SQLException **/
	public int ExecuteUpdate(PreparedStatement LoPrestm) throws SQLException {
		int cnt = 0;
		
		try {
			try {
				LoPrestm.executeUpdate();
			} catch (SQLException sqlex) {
				String cmethod = new Object(){}.getClass().getEnclosingMethod().getName();
				LastException.SetLastException(cmethod, "", sqlex);
				LastException.LogWrite();
				_Conec.rollback();
				throw sqlex;
			}
			_Conec.commit();
			LoPrestm.close();
		} catch (SQLException ex) {
			String cmethod = new Object(){}.getClass().getEnclosingMethod().getName();
			LastException.SetLastException(cmethod, "", ex);
			LastException.LogWrite();
			throw ex;
		}
		
		return cnt;
	}
	
	/** データベースコネクション
	 * @return java.sql.Connection **/
	public Connection getConec() {
		return this._Conec;
	}

	/** データベースコネクションの接続状態
	 * @return boolean **/
	public boolean getOpenFlag() {
		return this._OpenFlag;
	}
	
	/** OS統合認証=true、ユーザーIDとパスワード認証=false
	* @return boolean **/
	public boolean getLoginMode() {
		return this._LoginMode;
	}
	/** OS統合認証=true、ユーザーIDとパスワード認証=false
	 * @param value **/
	public void setLoginMode(boolean value) {
		this._LoginMode = value;
	}

	/** 接続タイムアウト設定
	* @return int **/
	public int getConnectTimeout() {
		return this._ConnectTimeout;
	}
	/** 接続タイムアウト設定
	 * @param value **/
	public void setConnectTimeout(int value) {
		this._ConnectTimeout = value;
	}

	/** SQLServerでMultipleActiveResultSetsを使用する=True、使用しない=False
	 * @return boolean **/
	public boolean getMultipleActiveResultSets() {
		return this._MultipleActiveResultSets;
	}
	/** SQLServerでMultipleActiveResultSetsを使用する=True、使用しない=False
	 * @param value **/
	public void setMultipleActiveResultSets(boolean value) {
		this._MultipleActiveResultSets = value;
	}	
}