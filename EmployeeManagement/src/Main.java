import java.awt.*;
import java.sql.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

// 사원 관리 프로그램
public class Main extends JFrame {
	private JButton btnInsert, btnDelete, btnUpdate, btnLeave, btnSelect, btnSearch, btnClear;
	private JTextField tfId, tfName, tfAge, tfAddress, tfDept, tfSalary, tfJoindate, tfSearch;
	private JRadioButton rbId, rbName, rbDept;
	Connection conn;
//	Statement stmt;
	PreparedStatement pstmt; /* PreparedStatement를 사용하여 SQL문 입력시 문법 오류를 없애고, 성능을 개선함 */
	ResultSet rs = null;
	
	// 결과 출력 화면용 JTable
	String header[] = {"ID", "이름", "나이", "주소", "부서", "연봉", "입사일", "퇴사일"};
	
	// JTable 셀 수정 불가능 하도록 설정
	DefaultTableModel model = new DefaultTableModel(header, 0) {
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};
	JTable empTable = new JTable(model);
	JScrollPane tblSP = new JScrollPane(empTable);
	
	// 생성자
	public Main() {
		// 창 제목
		super("사원 관리 프로그램");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// GUI 생성 메소드 호출
		createGUI();
		
		// 테이블 이벤트 처리 - 마우스 이벤트 리스너 (익명 클래스)
		empTable.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {
				// 셀 더블클릭시 선택한 행에 들어있는 속성을 JTextField에 표시함
				
				// DB를 사용한 값 가져오기
//				if (e.getClickCount() == 2) {
//					var selectedRow = empTable.getValueAt(empTable.getSelectedRow(), 0);
//					try {
//						conn = DBConn.dbConnection();
//						stmt = conn.createStatement();
//						rs = stmt.executeQuery("select * from emp where empid ='" + selectedRow + "';");
//						
//						while(rs.next()) {
//							tfId.setText(rs.getString("empid"));
//							tfName.setText(rs.getString("name"));
//							tfAge.setText(rs.getString("age"));
//							tfAddress.setText(rs.getString("address"));
//							tfDept.setText(rs.getString("dept"));
//							tfSalary.setText(rs.getString("salary"));
//							tfJoindate.setText(rs.getString("joindate"));
//						}
//						stmt.close(); conn.close();
//						
//					} catch(Exception ex) {
//						ex.printStackTrace();
//						System.err.println("- 테이블 이벤트 처리 오류 -");
//					}
//				}
				
				// JTable에 있는 값으로 가져오기
				// (JDBC 사용을 최소화 하기 위해 사용)
				if (e.getClickCount() == 2) {
					int selectedRow = empTable.getSelectedRow();
					Object rowValue[] = new Object[7];
					Object tfNames[] = {tfId, tfName, tfAge, tfAddress, tfDept, tfSalary, tfJoindate};
					for(int i=0; i<7; i++) 
						rowValue[i] = empTable.getValueAt(selectedRow, i);
					tfId.setText(rowValue[0].toString());
					tfName.setText(rowValue[1].toString());
					tfAge.setText(rowValue[2].toString());
					tfAddress.setText(rowValue[3].toString());
					tfDept.setText(rowValue[4].toString());
					tfSalary.setText(rowValue[5].toString());
					tfJoindate.setText(rowValue[6].toString());
				}
				
			}
		});
		
		// 버튼 이벤트 처리 - 액션 이벤트 리스너 (익명 클래스)
		btnInsert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dbInsert();
			}
		});
		btnUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dbUpdate();
			}
		});
		btnLeave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dbLeave();
			}
		});
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dbDelete();
			}
		});
		btnSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dbSelect();
			}
		});
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tfClear();
			}
		});
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dbSearch();
			}
		}); /* - 버튼 이벤트 처리 끝 - */
		
		// 초기 실행시 모든 데이터 조회
		dbSelect();
		
		// 창 크기 조절 불가
		setResizable(false);
		
		// 창 크기 설정
		setSize(850, 410);
		setVisible(true);
	}
	
	// GUI 생성 메소드
	public void createGUI() {
		// 메인 JPanel c
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		
		// c의 하위 패널 pLeft
		// 데이터 입력 부분 & 버튼 부분을 위한 왼쪽 JPanel
		JPanel pLeft = new JPanel();
		pLeft.setLayout(new BorderLayout());
		pLeft.setBorder(BorderFactory.createEmptyBorder(50, 5, 0, 5));
		
		// pLeft의 하위 패널 plNorth
		// 데이터 입력 부분 JPanel
		JPanel plNorth = new JPanel();
		plNorth.setLayout(new GridLayout(7, 2, 5, 5));
		plNorth.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));
		
		plNorth.add(new JLabel("사원번호"));
		tfId = new JTextField();
		plNorth.add(tfId);
		
		plNorth.add(new JLabel("이름"));
		tfName = new JTextField();
		plNorth.add(tfName);
		
		plNorth.add(new JLabel("나이"));
		tfAge = new JTextField();
		plNorth.add(tfAge);
		
		plNorth.add(new JLabel("주소"));
		tfAddress = new JTextField();
		plNorth.add(tfAddress);
		
		plNorth.add(new JLabel("부서"));
		tfDept = new JTextField();
		plNorth.add(tfDept);
		
		plNorth.add(new JLabel("연봉"));
		tfSalary = new JTextField();
		plNorth.add(tfSalary);
		
		plNorth.add(new JLabel("입사일  ex)2021-06-01"));
		tfJoindate = new JTextField();
		plNorth.add(tfJoindate);
		
		pLeft.add(plNorth, BorderLayout.NORTH);
		
		// pLeft의 하위패널 plMiddle
		// 버튼 + 검색 부분 JPanel
		JPanel plMiddle = new JPanel();
		plMiddle.setLayout(new BorderLayout());
		
		// plMiddle의 하위 패널 plBtn
		// 기능 수행을 위한 버튼이 모인 JPanel
		JPanel plBtn = new JPanel();
		plBtn.setLayout(new GridLayout(2, 3, 5, 5));
		btnInsert = new JButton("입력");
		btnUpdate = new JButton("수정");
		btnDelete = new JButton("삭제");
		btnLeave = new JButton("퇴사");
		btnSelect = new JButton("조회");
		btnClear = new JButton("지우기");
		plBtn.add(btnInsert);
		plBtn.add(btnUpdate);
		plBtn.add(btnDelete);
		plBtn.add(btnLeave);
		plBtn.add(btnSelect);
		plBtn.add(btnClear);
		
		// 상위 패널 plMiddle에 추가
		plMiddle.add(plBtn, BorderLayout.NORTH);
		
		// 상위 패널 pLeft에 추가
		pLeft.add(plMiddle, BorderLayout.CENTER);
		
		
		// c의 하위 패널 pRight
		// 검색 & 조회 JTable을 위한 오른쪽 JPanel
		JPanel pRight = new JPanel();
		pRight.setLayout(new BorderLayout());
		
		// 검색 부분을 위한 별도 JPanel
		tfSearch = new JTextField(18);
		rbId = new JRadioButton("사원번호", true);
		rbName = new JRadioButton("이름");
		rbDept = new JRadioButton("부서");
		btnSearch = new JButton("검색");
		ButtonGroup group = new ButtonGroup();
		group.add(rbId); group.add(rbName); group.add(rbDept);
		
		// prSeacrh의 하위 패널 pn1
		// pn1  = JTextField & JButton 
		JPanel pn1 = new JPanel();
		pn1.add(tfSearch);
		pn1.add(btnSearch);
		
		// prSearch의 하위 패널 pn2
		// pn2 = JRadioButton
		JPanel pn2 = new JPanel();
		pn2.add(rbId);
		pn2.add(rbName);
		pn2.add(rbDept);
		
		// pRight의 하위 패널 prSearch
		// 검색 부분 최종 구현(pn1 + pn2)
		JPanel prSearch = new JPanel(new BorderLayout(0, 0));
		prSearch.add(BorderLayout.NORTH, pn1);
		prSearch.add(BorderLayout.CENTER, pn2);
		TitledBorder tb = new TitledBorder("검색");
		prSearch.setBorder(tb);
		
		// 상위 패널 pRight에 추가
		pRight.add(prSearch, BorderLayout.NORTH);
			
		// pRight에 JTable 삽입
		pRight.add(tblSP, BorderLayout.CENTER);
		
		// JTable column 크기 조정
		empTable.getColumn("ID").setPreferredWidth(40);
		empTable.getColumn("이름").setPreferredWidth(60);
		empTable.getColumn("나이").setPreferredWidth(40);
		empTable.getColumn("주소").setPreferredWidth(120);
		empTable.getColumn("연봉").setPreferredWidth(40);
		
		// JTable Column 이동, 크기조절 불가
		empTable.getTableHeader().setReorderingAllowed(false);
		empTable.getTableHeader().setResizingAllowed(false);
		
		// JTable 셀에 가운데 정렬 적용
		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
		dtcr.setHorizontalAlignment(SwingConstants.CENTER);
		TableColumnModel tcm = empTable.getColumnModel();
		for(int i=0; i<empTable.getColumnCount(); i++)
			tcm.getColumn(i).setCellRenderer(dtcr);
		
		// 메인 패널 c에 하위 패널들 추가
		c.add(pLeft, BorderLayout.WEST);
		c.add(pRight, BorderLayout.CENTER);
	}
	
	// 삽입 SQL 메소드
	public void dbInsert() {
		try {
			conn = DBConn.dbConnection();
			// stmt = conn.createStatement();
			
			// 데이터를 입력받는 JTextField로부터 입력을 희망하는 데이터를 가져와 변수에 대입
			String id = tfId.getText().toString();
			String name = tfName.getText().toString();
			String age = tfAge.getText().toString();
			String address = tfAddress.getText().toString();
			String dept = tfDept.getText().toString();
			String salary = tfSalary.getText().toString();
			String joindate = tfJoindate.getText().toString();
			
			// 데이터 삽입 SQL문 실행
			String insertSQL = "insert into emp values(?, ?, ?, ?, ?, ?, ?, NULL);";
			pstmt = conn.prepareStatement(insertSQL);
			// SQL)) id, age, salary는 int 자료형이지만 varchar 자료형과 같은 따옴표 '' 방식으로 입력될 수 있음
			pstmt.setString(1, id);
			pstmt.setString(2, name);
			pstmt.setString(3, age);
			pstmt.setString(4, address);
			pstmt.setString(5, dept);
			pstmt.setString(6, salary);
			pstmt.setString(7, joindate);
			pstmt.executeUpdate();
			
//			stmt.executeUpdate("insert into emp values(" + id + ", '" + name + "', " + age + 
//					", '" + address + "', '" + dept + "', " + salary + ", '" + joindate + "', null);");
			System.out.println("ID: " + id + " 이름: " + name + " - 입력 완료\n");
			
			
			// 데이터 삽입 완료 후 JTextField 초기화
			tfClear();
			
//			stmt.close(); 
			pstmt.close();
			conn.close();
			dbSelect();
			
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("이미 등록된 사원번호 입니다.");
			System.err.println("- 입력 에러 발생 -");
		}
	}
	
	// 수정 SQL 메소드
	public void dbUpdate() {
		try {
			conn = DBConn.dbConnection();
//			stmt = conn.createStatement();
			
			// DB에서 가져올 데이터 속성을 저장할 변수
			var name = ""; var age = ""; var address = ""; var dept = ""; 
			var salary  = ""; var joindate = "";
			
			// TextField에 입력된 정보 저장
			var in_id = tfId.getText().toString();
			var in_name = tfName.getText().toString();
			var in_age = tfAge.getText().toString();
			var in_address = tfAddress.getText().toString();
			var in_dept = tfDept.getText().toString();
			var in_salary = tfSalary.getText().toString();
			var in_joindate = tfJoindate.getText().toString();
			
			String selectSQL = "select * from emp where empid = ?";
			pstmt = conn.prepareStatement(selectSQL);
			pstmt.setString(1, in_id);
			rs = pstmt.executeQuery();
//			rs = stmt.executeQuery("select * from emp where empid = '" + in_id + "';");
			
			while(rs.next()) {
				name = rs.getString("name");
				age = rs.getString("age");
				address = rs.getString("address");
				dept = rs.getString("dept");
				salary  = rs.getString("salary");
				joindate = rs.getString("joindate");
			}
			
			// 사용자가 JTextField에 입력한 정보가 있는지 체크 후 입력한 정보가 있다면 기존 데이터에 덮어쓰기 후 update 실행
			// JTable 이벤트 처리를 통해 덮어쓰기 방식으로 데이터를 수정하므로 그닥 쓸모 없음
//			if(in_name.length() > 0) name = in_name; 
//			if(in_age.length() > 0) age = in_age;
//			if(in_address.length() > 0) address = in_address;
//			if(in_dept.length() > 0) dept = in_dept;
//			if(in_salary.length() > 0) salary = in_salary;
//			if(in_joindate.length() > 0) joindate = in_joindate;
			
			// 불필요한 덮어쓰기를 줄이기 위해 개선된 조건식
			if(!in_name.equals(name) && in_name.length() != 0) name = in_name;
			if(!in_age.equals(age) && in_age.length() != 0) age = in_age;
			if(!in_address.equals(address) && in_address.length() != 0) address = in_address;
			if(!in_dept.equals(dept) && in_dept.length() != 0) dept = in_dept;
			if(!in_salary.equals(salary) && in_salary.length() != 0) salary = in_salary;
			if(!in_joindate.equals(joindate) && in_joindate.length() != 0) joindate = in_joindate;
			
			String updateSQL = "update emp set name = ?, age = ?, address = ?, dept = ?, salary = ?, joindate = ? where empid = ?;";
			pstmt = conn.prepareStatement(updateSQL);
			pstmt.setString(1, name);
			pstmt.setString(2, age);
			pstmt.setString(3, address);
			pstmt.setString(4, dept);
			pstmt.setString(5, salary);
			pstmt.setString(6, joindate);
			pstmt.setString(7, in_id);
			pstmt.executeUpdate();
			
//			stmt.executeUpdate("update emp set name = '" + name + "', age = " + age + ", address = '" + 
//					address + "', dept = '" + dept + "', salary = " + salary + ", joindate = '" + 
//					joindate + "' where empid = " + in_id + ";");
			
			System.out.println("ID: " + in_id + " - 수정 완료\n");
			
			// 데이터 수정 완료 후 JTextField 초기화
			tfClear();
			
//			stmt.close(); 
			rs.close();
			pstmt.close();
			conn.close();
			dbSelect();
		} catch (Exception e) {
			// e.printStackTrace();
			System.err.println("- 수정 에러 발생 -");
		}
	}
	
	// 퇴사 SQL 메소드
	public void dbLeave() {
		try {
			conn = DBConn.dbConnection();
//			stmt = conn.createStatement();
			
			String inputid = tfId.getText().toString();
			
			// 현재 날짜로 퇴사 처리 (YYYY-MM-DD)
			String updateSQL = "UPDATE emp SET leavedate = CURRENT_DATE() WHERE empid = ?";
			pstmt = conn.prepareStatement(updateSQL);
			pstmt.setString(1, inputid);
			pstmt.executeUpdate();
			
//			stmt.executeUpdate("update emp set leavedate = current_date() where empid = " + inputid + ";");
			System.out.println("ID: " + inputid + " - 퇴사처리 완료\n");
			
			// 퇴사처리 완료 후 JTextField 초기화
			tfClear();
			
//			stmt.close(); 
			pstmt.close();
			conn.close();
			dbSelect();
		} catch(Exception e) {
			// e.printStackTrace();
			System.err.println("- 퇴사처리 에러 발생 -");
		}
	}
		
	// 삭제 SQL 메소드
	public void dbDelete() {
		try {
			conn = DBConn.dbConnection();
//			stmt = conn.createStatement();
			
			// JTextField로부터 삭제를 희망하는 ID를 가져와 String타입 변수에 대입
			String inputid = tfId.getText().toString();
			
			// 데이터 삭제 SQL문 실행
			String deleteSQL = "delete from emp where empid = ?";
			pstmt = conn.prepareStatement(deleteSQL);
			pstmt.setString(1, inputid);
			pstmt.executeUpdate();
			
//			stmt.executeUpdate("delete from emp where empid = " + inputid);
			System.out.println("ID: " + inputid + " - 삭제 완료\n");
			
			// 데이터 삭제 완료 후 JTextField 초기화
			tfClear();
			
//			stmt.close(); 
			pstmt.close();
			conn.close();
			dbSelect();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("- 삭제 에러 발생 -");
		}
	}
	

	// 전체 조회 SQL 메소드
	public void dbSelect() {
		try {
			// JTable 클리어
			model.setRowCount(0);
			
			conn = DBConn.dbConnection();
//			stmt = conn.createStatement();
			
			// ResultSet를 통해 데이터 읽어옴
//			rs = stmt.executeQuery("select * from emp;");
			
			// empid를 세자릿수로 표사하기 위해 LPAD()이용
			String selectSQL = "select LPAD(empid, 3, '0') as empid, name, age, address, dept, salary, joindate, leavedate from emp;";
			pstmt = conn.prepareStatement(selectSQL);
			rs = pstmt.executeQuery();
			
//			rs = stmt.executeQuery("select LPAD(empid, 3, '0') as empid, name, age, address, dept, salary, joindate, leavedate from emp;");
			
			// next()를 이용해 마지막 행 까지 데이터 출력
			while(rs.next()) {
				var id = rs.getString("empid");
				var name = rs.getString("name");
				var age = rs.getString("age");
				var address = rs.getString("address");
				var dept = rs.getString("dept");
				var salary  = rs.getString("salary");
				var joindate = rs.getString("joindate");
				var leavedate = rs.getString("leavedate");
				
				Object data[] = {id, name, age, address, dept, salary, joindate, leavedate};
				// 한 행 씩 테이블에 데이터 삽입
				model.addRow(data);
			}
			
			System.out.println("전체 조회 완료");
//			stmt.close();
			rs.close();
			pstmt.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("- 전체 조회 에러 발생 -");
		}
	}
	
	// 조건 조회 SQL 메소드
	public void dbSearch() {
		try {
			// JTable 클리어
			model.setRowCount(0);
			
			conn = DBConn.dbConnection();
//			stmt = conn.createStatement();
			
			// JTextField에서 검색 키워드를 가져옴
			var searchText = tfSearch.getText().toString();
			String searchSQL = null;
			
			// 어느 JRadioButton이 선택 되어있는지에 따라 다르게 조회
			// empid를 세자릿수로 표사하기 위해 LPAD()이용
			if(rbId.isSelected()) {
				searchSQL = "select LPAD(empid, 3, '0') as empid, name, age, address, dept, salary, joindate, leavedate from emp where empid like '" + "%" + searchText + "%" + "';";
			} else if(rbName.isSelected()) {
				searchSQL = "select LPAD(empid, 3, '0') as empid, name, age, address, dept, salary, joindate, leavedate from emp where name like '" + "%" + searchText + "%" + "';";
			} else if(rbDept.isSelected()) {
				searchSQL = "select LPAD(empid, 3, '0') as empid, name, age, address, dept, salary, joindate, leavedate from emp where dept like '" + "%" + searchText + "%" + "';";
			}
			
			// ResultSet를 통해 데이터 읽어옴
			pstmt = conn.prepareStatement(searchSQL);
			rs = pstmt.executeQuery();
//			rs = stmt.executeQuery(searchSQL);
			
			// next()를 이용해 마지막 행 까지 데이터 출력
			while(rs.next()) {
				var id = rs.getString("empid");
				var name = rs.getString("name");
				var age = rs.getString("age");
				var address = rs.getString("address");
				var dept = rs.getString("dept");
				var salary  = rs.getString("salary");
				var joindate = rs.getString("joindate");
				var leavedate = rs.getString("leavedate");
							
				Object data[] = {id, name, age, address, dept, salary, joindate, leavedate};
				// 한 행 씩 테이블에 데이터 삽입
				model.addRow(data);
			}
			
			System.out.println("조건 조회 완료");
//			stmt.close();
			rs.close();
			pstmt.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("- 조건 조회 에러 발생 -");
		}
	}
	
	// 입력 필드 초기화 메소드
	public void tfClear() {
		tfId.setText("");
		tfName.setText("");
		tfAge.setText("");
		tfAddress.setText("");
		tfDept.setText("");
		tfSalary.setText("");
		tfJoindate.setText("");
	}
	
	public static void main(String[] args) {
		System.out.println("- 사원 관리 프로그램 -\n");
		new Main();
	}
}
