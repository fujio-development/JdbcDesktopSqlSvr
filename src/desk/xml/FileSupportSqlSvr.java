package desk.xml;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;

import desk.LastException;
import desk.db.JdbcSqlSvr;

public class FileSupportSqlSvr extends JdbcSqlSvr {
	private SqlSvrInfo mMssqlSvr;
	private String iHoldPath;
	private String iHoldFile;
	
	public FileSupportSqlSvr() {
		mMssqlSvr = new SqlSvrInfo();
	}
	
	/** Microsoft SQLServerに外部設定ファイルを使って接続します
	 * @param pSetPath 外部設定ファイルのフォルダ階層
	 * @param pSetFile 外部設定ファイルのフォルダ名
	 * @return boolean true=OK、false=ファイル無しエラー
	 * @throws ClassNotFoundException
	 * @throws SQLException **/
	public boolean Open(String pSetPath, String pSetFile) throws ClassNotFoundException, SQLException, FileNotFoundException {
		boolean success = true;
		
		iHoldPath = pSetPath;
		iHoldFile = pSetFile;
		File file = new File(iHoldPath + iHoldFile);
		if (file.exists()) {
			Deserialize();
			AccessorSet();
			ConecString();
			GoOpen();
		} else {
			success = false;
		}
		
		return success;
	}
	
	/** 標準的な内容で目的のファイルを作成します<br />
	 *  ※Openメソッドを実行した引数で作成
	 * @return boolean true=OK、false=ファイル無しエラー **/
	public boolean Create() throws FileNotFoundException {
		return Create(iHoldPath, iHoldFile);
	}
	
	/** 標準的な内容で目的のファイルを作成します
	 * @param pSetPath
	 * @param pSetFile
	 * @return boolean true=OK、false=ファイル無しエラー **/
	public boolean Create(String pSetPath, String pSetFile) throws FileNotFoundException{
		boolean success = true;
		
		iHoldPath = pSetPath;
		iHoldFile = pSetFile;
		File file = new File(iHoldPath + iHoldFile);
		if (file.exists()) {
			SerializeDefaultData();
			Serialize();
		} else {
			success = false;
		}
		
		return success;
	}
	
	/** 外部設定ファイルからの設定値を読み込みます**/
	private void AccessorSet() {
		_Host = mMssqlSvr.getMssqlSvHost();
		_Instance = mMssqlSvr.getMssqlSvInstance();
		_Port = mMssqlSvr.getMssqlSvPort();
		_Catalog = mMssqlSvr.getMssqlSvCatalog();
		_LoginMode = mMssqlSvr.getMssqlSvLoginMode();
		_UserID = mMssqlSvr.getMssqlSvUserID();
		_Password = mMssqlSvr.getMssqlSvPassword();
		_ConnectTimeout = mMssqlSvr.getMssqlSvConnectTimeout();
		_MultipleActiveResultSets = mMssqlSvr.getMssqlSvMars();
	}
	
	/** 外部設定ファイルが無い場合、標準的な内容で作成するための値を書き込みます**/
	private void SerializeDefaultData() {
		mMssqlSvr.setMssqlSvHost("(local)");
		mMssqlSvr.setMssqlSvInstance("SQLEXPRESS");
		mMssqlSvr.setMssqlSvPort(1433);
		mMssqlSvr.setMssqlSvCatalog("TestDatabase");
		mMssqlSvr.setMssqlSvLoginMode(true);
		mMssqlSvr.setMssqlSvUserID("sa");
		mMssqlSvr.setMssqlSvPassword("sapassword");
		mMssqlSvr.setMssqlSvConnectTimeout(15);
		mMssqlSvr.setMssqlSvMars(true);
	}
	
	/** XMLファイルへシリアル化し標準的な内容で書き込みます
	 *  **/
	private void Serialize() throws FileNotFoundException {
		try (XMLEncoder Xmencoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(iHoldPath + iHoldFile)));) {	
			Xmencoder.writeObject(mMssqlSvr);
		} catch (FileNotFoundException fileex) {
			String cmethod = Thread.currentThread().getStackTrace()[1].getMethodName();
			LastException.SetLastException(cmethod, iHoldPath + iHoldFile, fileex);
			LastException.LogWrite();
			throw fileex;
		}
	}
	
	/** XMLファイルを逆シリアル化し読み込みます
	 *  **/
	private void Deserialize() throws FileNotFoundException {
		try (XMLDecoder Xmdecoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(iHoldPath + iHoldFile)));) {	
			mMssqlSvr = (SqlSvrInfo)Xmdecoder.readObject();
		} catch (FileNotFoundException fileex) {
			String cmethod = Thread.currentThread().getStackTrace()[1].getMethodName();
			LastException.SetLastException(cmethod, iHoldPath + iHoldFile, fileex);
			LastException.LogWrite();
			throw fileex;
		}
	}
}