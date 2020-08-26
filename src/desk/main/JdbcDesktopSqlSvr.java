package desk.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import desk.LastException;
import desk.xml.FileSupportSqlSvr;

public class JdbcDesktopSqlSvr implements Runnable {

	private boolean DbOpenType = true;
	private JFrame Fwindow = new JFrame();
	private JPanel Fpanel = new JPanel();
	private JTable Table1;
	private JScrollPane Scroll1;
	private DefaultTableModel DtableModel;
	private String[] headers = {"商品ID", "商品番号", "商品名", "編集日付", "編集時刻", "備考"};
	private JTextArea LabelArea = new JTextArea();
	private JButton ButtonQuery = new JButton();
	private JButton ButtonInsert = new JButton();
	private JButton ButtonUpdate = new JButton();
	private JButton ButtonDelete = new JButton();
	private JLabel Label1 = new JLabel();
	private JLabel Label2 = new JLabel();
	private JLabel Label3 = new JLabel();
	private JLabel Label4 = new JLabel();
	private JLabel LabelNumId = new JLabel();
	private JTextField TextShohinNum = new JTextField();
	private JTextField TextShohinName = new JTextField();
	private JTextField TextNote = new JTextField();
	
	public static void main(String[] args) {
		Thread thread = new Thread(new JdbcDesktopSqlSvr());
		thread.setUncaughtExceptionHandler(new OriginalUncaughtException());
        thread.start();
	}
	
	@Override
	public void run() {
		JdbcDesktopSqlSvr jdm = new JdbcDesktopSqlSvr();
		jdm.FrameDesignSetting();
		jdm.TableSetting();
		
		jdm.ButtonQuery.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				jdm.DtableModel.setRowCount(0); //表示行クリア
				String sqlstr = "select * from ShohinDataDesk";
				List<ShohinDto> list = new ArrayList<ShohinDto>();
				int cnt = 0;
				
				try (FileSupportSqlSvr sqlserver = new FileSupportSqlSvr();) {
					jdm.DatabaseOpen(sqlserver);
					list = sqlserver.ExecuteQuery(ShohinDto.class, sqlstr);
					for (int i = 0; i < list.size(); i++) {
			            String ldate = ((ShohinDto)list.get(i)).getEditDate().toString();
			            ldate = ldate.substring(0,4) + "/" + ldate.substring(4,6) + "/" + ldate.substring(6,8);
			            String ltime = ((ShohinDto)list.get(i)).getEditTime().toString();
			            ltime = String.format("%6s", ltime).replace(" ", "0");
			            ltime = ltime.substring(0,2) + ":" + ltime.substring(2,4) + ":" + ltime.substring(4,6);
			            Object[] Objrs = {((ShohinDto)list.get(i)).getNumId(),
			            		((ShohinDto)list.get(i)).getShohinNum(),
			            		((ShohinDto)list.get(i)).getShohinName(),
			            			ldate,
			            			ltime,
			            		((ShohinDto)list.get(i)).getNote()};
			            jdm.DtableModel.addRow(Objrs);
					}
					cnt = sqlserver.ExecuteCount("select count(*) cnt from ShohinDataDesk");
				} catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException | 
						ClassNotFoundException | FileNotFoundException | NoSuchMethodException ex) {
					JOptionPane.showMessageDialog(jdm.Fwindow, "システムエラーが発生しました。\nアプリケーションを終了します。\n" , "メッセージ", 2);
					jdm.Fwindow.dispose();
					return;
				}
				jdm.TableSetting();
	            jdm.TextFieldClear();
				jdm.LabelArea.append("全件表示しました。件数は" + String.valueOf(cnt) + "です。\n");
			}
		});
		
		jdm.ButtonInsert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				String sqlstr = "";
				
				if (jdm.TextShohinNum.getText().matches("^[0-9]{1,4}$") == false) {
					JOptionPane.showMessageDialog(jdm.Fwindow, "商品番号は半角数値の0～9999でなければなりません。","メッセージ", 2);
					return;
				}
				
				sqlstr = "insert into ShohinDataDesk (ShohinNum, ShohinName, EditDate, EditTime, Note) ";
				sqlstr += "values (?, ?, ?, ?, ?)";
				try (FileSupportSqlSvr sqlserver = new FileSupportSqlSvr();) {
					jdm.DatabaseOpen(sqlserver);
					try (PreparedStatement pstm = sqlserver.getConec().prepareStatement(sqlstr);) {
						pstm.setShort(1, Short.valueOf(jdm.TextShohinNum.getText()));
						pstm.setString(2, jdm.TextShohinName.getText());
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
						String daytime = formatter.format(java.time.LocalDateTime.now());
						pstm.setBigDecimal(3, BigDecimal.valueOf(Integer.valueOf(daytime.substring(0,8))));
						pstm.setBigDecimal(4, BigDecimal.valueOf(Integer.valueOf(daytime.substring(8))));
						pstm.setString(5, jdm.TextNote.getText());
						sqlserver.ExecuteUpdate(pstm);
					} 
				} catch (SQLException | ClassNotFoundException | FileNotFoundException ex) {
					JOptionPane.showMessageDialog(jdm.Fwindow, "システムエラーが発生しました。\nアプリケーションを終了します。\n" , "メッセージ", 2);
					jdm.Fwindow.dispose();
					return;
				}
				jdm.TextFieldClear();
				jdm.LabelArea.append("1件追加しました。\n");
			}
		});
		
		
		jdm.ButtonUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				String sqlstr = "";
				int cnt = jdm.DtableModel.getRowCount();
				
				if (cnt <= 0 || jdm.LabelNumId.getText().equals("")) {
					JOptionPane.showMessageDialog(jdm.Fwindow, "更新する商品行が選択できていません。","商品IDなし", 2);
					return;
				}
				
				if (jdm.TextShohinNum.getText().matches("^[0-9]{1,4}$") == false) {
					JOptionPane.showMessageDialog(jdm.Fwindow, "商品番号は半角数値の0～9999でなければなりません。","メッセージ", 2);
					return;
				}
				
				sqlstr = "update ShohinDataDesk set ShohinNum=?, ShohinName=?";
				sqlstr += ", EditDate=?, EditTime=?, Note=? where NumId=?";
				try (FileSupportSqlSvr sqlserver = new FileSupportSqlSvr();) {
					jdm.DatabaseOpen(sqlserver);
					try (PreparedStatement pstm = sqlserver.getConec().prepareStatement(sqlstr);) {
						pstm.setShort(1, Short.valueOf(jdm.TextShohinNum.getText()));
						pstm.setString(2, jdm.TextShohinName.getText());
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
						String daytime = formatter.format(java.time.LocalDateTime.now());
						pstm.setBigDecimal(3, BigDecimal.valueOf(Integer.valueOf(daytime.substring(0,8))));
						pstm.setBigDecimal(4, BigDecimal.valueOf(Integer.valueOf(daytime.substring(8))));
						pstm.setString(5, jdm.TextNote.getText());
						pstm.setInt(6, Integer.valueOf(jdm.LabelNumId.getText()));
						sqlserver.ExecuteUpdate(pstm);
					}
				} catch (SQLException | ClassNotFoundException | FileNotFoundException ex) {
					JOptionPane.showMessageDialog(jdm.Fwindow, "システムエラーが発生しました。\nアプリケーションを終了します。\n" , "メッセージ", 2);
					jdm.Fwindow.dispose();
					return;
				}
				jdm.TextFieldClear();
				jdm.LabelArea.append("選択された商品を更新しました。\n");
			}
		});

		jdm.ButtonDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				String sqlstr = "";
				int cnt = jdm.DtableModel.getRowCount();
				if (cnt <= 0 || jdm.LabelNumId.getText().equals("")) {
					JOptionPane.showMessageDialog(jdm.Fwindow, "更新する商品行が選択できていません。","商品IDなし", 2);
					return;
				}
				
				if (jdm.TextShohinNum.getText().matches("^[0-9]{1,4}$") == false) {
					JOptionPane.showMessageDialog(jdm.Fwindow, "商品番号は半角数値の0～9999でなければなりません。","メッセージ", 2);
					return;
				}
				
				sqlstr = "delete from ShohinDataDesk where NumId = ?";
				try (FileSupportSqlSvr sqlserver = new FileSupportSqlSvr();) {
					jdm.DatabaseOpen(sqlserver);
					try (PreparedStatement pstm = sqlserver.getConec().prepareStatement(sqlstr);) {
						pstm.setInt(1, Integer.valueOf(jdm.LabelNumId.getText()));
						sqlserver.ExecuteUpdate(pstm);
					}
				} catch (SQLException | ClassNotFoundException | FileNotFoundException ex) {
					JOptionPane.showMessageDialog(jdm.Fwindow, "システムエラーが発生しました。\nアプリケーションを終了します。\n" , "メッセージ", 2);
					jdm.Fwindow.dispose();
					return;
				}
				jdm.TextFieldClear();
				jdm.LabelArea.append("商品ID：" + String.valueOf(jdm.LabelNumId.getText()) + "の行を削除しました。\n");
			}
		});
		
		jdm.Table1.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent listevent) {
				if(listevent.getValueIsAdjusting()) {
					return;
				}
				jdm.LabelNumId.setText(((Integer) jdm.Table1.getValueAt(jdm.Table1.getSelectedRow(), 0)).toString());
				jdm.TextShohinNum.setText(((Short) jdm.Table1.getValueAt(jdm.Table1.getSelectedRow(), 1)).toString());
				jdm.TextShohinName.setText(jdm.Table1.getValueAt(jdm.Table1.getSelectedRow(), 2).toString());
				jdm.TextNote.setText(jdm.Table1.getValueAt(jdm.Table1.getSelectedRow(), 5).toString());
			}
		});
		//sqlserver.Create("./","MsSqlServer.xml");
		//jm.fwindow.add(jdm.fpanel);
		jdm.Fwindow.getContentPane().add(jdm.Fpanel,BorderLayout.CENTER);
		jdm.Fwindow.setVisible(true);
	}
	
	private void FrameDesignSetting() {
		Fwindow.setTitle("Jdbc Driver(直接) + デスクトップアプリ + SQLServer");
		Fwindow.setLocation(500,200);
		Fwindow.setSize(800, 600);
		Fwindow.setUndecorated(false); // タイトルバー表示・非表示
		Fwindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		DtableModel = new DefaultTableModel(headers, 0);
		Table1 = new JTable(DtableModel);
		Scroll1 = new JScrollPane(Table1);
		Scroll1.setBounds(25,25,700,200);
		Fpanel.setPreferredSize(new Dimension(700, 100));
		Fpanel.add(Scroll1);
		LabelArea.setText("");
		LabelArea.setBounds(25,235,350,100);
		LabelArea.setFocusable(false);
		Fpanel.add(LabelArea);

		ButtonQuery.setText("抽出");
		ButtonQuery.setBounds(50,470,150,50);
		Fpanel.add(ButtonQuery);
		ButtonInsert.setText("追加");
		ButtonInsert.setBounds(230, 470, 150, 50);
		Fpanel.add(ButtonInsert);
		ButtonUpdate.setText("更新");
		ButtonUpdate.setBounds(410, 470, 150, 50);
		Fpanel.add(ButtonUpdate);
		ButtonDelete.setText("削除");
		ButtonDelete.setBounds(590, 470, 150, 50);
		Fpanel.add(ButtonDelete);

		Label1.setText("商品ID：");
		Label1.setBounds(400,250,100,25);
		Fpanel.add(Label1);
		Label2.setText("商品番号：");
		Label2.setBounds(400,300,100,25);
		Fpanel.add(Label2);
		Label3.setText("商品名：");
		Label3.setBounds(400,350,100,25);
		Fpanel.add(Label3);
		Label4.setText("備考：");
		Label4.setBounds(400,400,100,25);
		Fpanel.add(Label4);
		
		LabelNumId.setBounds(670,250,60,25);
		LabelNumId.setHorizontalAlignment(JLabel.RIGHT);
		Fpanel.add(LabelNumId);
		TextShohinNum.setBounds(600,300, 150,25);
		Fpanel.add(TextShohinNum);
		TextShohinName.setBounds(550,350,200,25);
		Fpanel.add(TextShohinName);
		TextNote.setBounds(450,400,300,25);
		Fpanel.add(TextNote);
		Fpanel.setLayout(null);
	}

	private void TextFieldClear() {
		LabelNumId.setText("");
		TextShohinNum.setText("");
		TextShohinName.setText("");
		TextNote.setText("");
	}
	
	private void DatabaseOpen(FileSupportSqlSvr sqlserver) throws SQLException, ClassNotFoundException, FileNotFoundException {
		
		if (DbOpenType) {
			if (sqlserver.Open("./","MsSqlServer.xml") == false) {
				JOptionPane.showMessageDialog(Fwindow, "データベース設定ファイルがありません。アプリケーションを終了します。\n", "メッセージ", 2);
				Fwindow.dispose();
			}
		} else {
			sqlserver.setHost("localhost");
			sqlserver.setInstance("SQLEXPRESS");;
			sqlserver.setCatalog("JdbcSample");
			sqlserver.setLoginMode(false);
			sqlserver.setUserID("sa");
			sqlserver.setPassword("sapassword");
			sqlserver.setConnectTimeout(3);
			sqlserver.setMultipleActiveResultSets(false);
			sqlserver.Open();
		}
	}
	
	private void TableSetting() {
		Table1.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		TableColumn col = Table1.getColumnModel().getColumn(0);
		col.setPreferredWidth(5);
		col = Table1.getColumnModel().getColumn(1);
		col.setPreferredWidth(5);
		col = Table1.getColumnModel().getColumn(2);
		col.setPreferredWidth(20);
		col = Table1.getColumnModel().getColumn(3);
		col.setPreferredWidth(5);
		col = Table1.getColumnModel().getColumn(4);
		col.setPreferredWidth(5);
		/**col = Table1.getColumnModel().getColumn(5);
		col.setPreferredWidth(35);**/
	}

}