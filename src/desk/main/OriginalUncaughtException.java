package desk.main;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import desk.LastException;

public class OriginalUncaughtException extends LastException implements UncaughtExceptionHandler {
	//集約例外プロシージャー
	@Override
	public void uncaughtException (Thread thread, Throwable throwable) {
		//例外の内容やトレース内容をLogに出力したい場合やユーザーに画面出力したい場合にここへ書きます。
		
		_LastExcepTitle = throwable.getClass().getName();
		_LastExcepPlace = Thread.currentThread().getStackTrace()[1].getMethodName();
		_LastExcepParam = "";
		_LastExcepMessage = throwable.getMessage();
		var sw = new StringWriter();
		try(var pw = new PrintWriter(sw);) {
			throwable.printStackTrace(pw);
			pw.flush();
			_LastExcepTrace = sw.toString();
		}
		
		LogWrite();
	}
}