package home.costin.util;

import java.sql.*;
import java.util.Calendar;

import sexpr.SExpr;

/**
 * Utility class for formatting JDBC resultSets
 * @author Costin Cozianu <a href=mailto://ccozianu@yahoo.com>ccozianu@yahoo.com</a>
 */
public class JDBCUtil {
	
	public static class EmptyResult extends SQLException {
		
	}
	
	public static void printHeader( ResultSet rs, java.io.PrintWriter print) 
	throws java.io.IOException, java.sql.SQLException 
	{
		java.sql.ResultSetMetaData rsmeta= rs.getMetaData();
		int columnCount= rsmeta.getColumnCount();

		StringBuffer header1= new StringBuffer(100),
					 header2= new StringBuffer(100),
					 header3= new StringBuffer(100);
					 
		for (int i=1;i<=columnCount;i++) {
			
			int colsize= rsmeta.getColumnDisplaySize(i);
			if (colsize<15) colsize=15;

			header1.append(' ').append( StringFormatter.formatString(
								rsmeta.getColumnName(i),colsize, StringFormatter.CENTERALIGN))
					.append(" |");
			header2.append(' ').append( StringFormatter.formatString(
								rsmeta.getColumnTypeName(i),colsize,StringFormatter.CENTERALIGN))
					.append(" |");

//			header3.append(" | ").append( StringFormatter.formatString(
//								rsmeta.get,colsize,StringFormatter.CENTERALIGN));
		}
		print.print(header1);print.println();
		print.println(header2);print.println();

	}
	public static void printResults( ResultSet rs, java.io.PrintWriter printer) 
	throws java.io.IOException, java.sql.SQLException
	{

		java.sql.ResultSetMetaData rsmeta= rs.getMetaData();
		int columnCount= rsmeta.getColumnCount();

		StringBuffer linebuf= new StringBuffer(100);

		String scol=null;
		if (! rs.next()) System.out.println("0 rows in result set");
		else do {
			linebuf.setLength(0);
			for (int i=1; i<=columnCount; i++) {
				int colsize=rsmeta.getColumnDisplaySize(i); if (colsize<15) colsize=15;
				linebuf.append(' ');
				scol= rs.getString(i);
				if ( rs.wasNull()) scol="NULL";
				linebuf.append( StringFormatter.formatString(scol,colsize,StringFormatter.CENTERALIGN))
						.append(" |");
			}
			printer.print(linebuf);printer.println();
		} while (rs.next());
	}
	
	public static Integer retrieveOneIntStrict(Connection connection, String stmt, Object ... args) throws SQLException {
		PreparedStatement ps = connection.prepareStatement(stmt);
		bindParams(ps,args);
		Integer result;
		ResultSet rs= ps.executeQuery();
		if (rs.next()) {
			result= rs.getInt(1);
			if (rs.wasNull()) result= null;
		}
		else throw new EmptyResult(); 
		return result;
	}
	
	
	/**
	 * returns one Integere from a (1,1) dimensional result
	 * or null if the result is empty or the returned cell has a null value 
	 */
	public static Integer maybeRetrieveOneInt(Connection connection, String stmt, Object ... args) throws SQLException {
		PreparedStatement ps = connection.prepareStatement(stmt);
		bindParams(ps,args);
		Integer result;
		ResultSet rs= ps.executeQuery();
		if (rs.next()) {
			result= rs.getInt(1);
			if (rs.wasNull()) result= null;
		}
		else result=null; 
		return result;
	}

	
	private static void bindParams(PreparedStatement ps, Object ... args) throws SQLException  {
		// TODO Auto-generated method stub
		for (int i = 0; i < args.length; i++) {
			Object obj = args[i];
			if(obj==null) ps.setNull(i+1,Types.VARCHAR);
			else
				bindValue(ps, i+1, obj);
		}
	}
	private static void bindValue(PreparedStatement ps, int i, Object obj) throws SQLException {
		if (obj instanceof Boolean) {
			ps.setBoolean(i,((Boolean) obj).booleanValue());
		}
		else if (obj instanceof Integer) {
			ps.setInt(i,((Integer) obj).intValue());
		}
		else if (obj instanceof Calendar) {
			ps.setDate(i, new Date(((Calendar) obj).getTimeInMillis()));
		}
		else 
			ps.setString(i,obj.toString());
	}
	public static int doUpdate(Connection connection, String stmt, Object ... args ) throws SQLException {
		PreparedStatement ps = connection.prepareStatement(stmt);
		bindParams(ps,args);
		return ps.executeUpdate();
	}
	public static ResultSet query(Connection connection, String stmt, Object ... args) throws SQLException {
		PreparedStatement ps = connection.prepareStatement(stmt);
		bindParams(ps,args);
		return ps.executeQuery();
	}
	
	public static SExpr retrieveSExpr(Connection connection, String stmt, Object ... args) throws SQLException {
		class SExprResultSetReader {
			ResultSet rs;
			ResultSetMetaData rsMeta;
			public SExprResultSetReader(ResultSet rs_) throws SQLException {
				this.rs= rs_;
				this.rsMeta= rs.getMetaData();
			}
			SExpr readOneRow() throws SQLException {
				SExpr.ListConstructor rc= new SExpr.ListConstructor();
				for (int i=1; i<= rsMeta.getColumnCount(); i++) {
					String s= rs.getString(i);
					rc.append(rs.wasNull()? SExpr.NIL : SExpr.make(s));
				}
				return rc.make();
			}
			
			SExpr _ () throws SQLException {
				SExpr.ListConstructor lc= new SExpr.ListConstructor();
				while (rs.next()) { lc.append(readOneRow());}
				return lc.make();
			}
		}
		
		PreparedStatement ps= connection.prepareStatement(stmt);
		bindParams(ps,args);
		
		return new SExprResultSetReader(ps.executeQuery())._();
	}
	
	
}
