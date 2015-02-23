package wiki;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

import sexpr.SExpr;
import sexpr.SExpr.ListConstructor;
import sexpr.util.SExpressions;

import home.costin.util.JDBCUtil;
import home.costin.util.StringConstructor;


/**
 * all the metadata operations and queries go here
 * @author ccozianu
 */

public class WikiMeta {
	
	/**
	 * to be replaced by a periodical team ranking algorithm
	 */
	private static String preferredTeam="0"; 

	/**
	 * verifies the the given version is good for Save,
	 * if null is passed in
	 */
	public static Integer checkPageVersionForSave(String page, String team, String user, Integer version) throws SQLException {
		Integer result;
		int count= JDBCUtil.doUpdate(WikiFilter.dbConnection(),
									"update wiki_pages_versions " +
										" set last_version = last_version + 1 " +
										" where page_name=? and team_name= ?  and last_version = ? "
									,page,team, version );
		if (count==0) { // may be an empty page create it
			if (version == 0) {
				//insert if not already exists
				JDBCUtil.doUpdate(WikiFilter.dbConnection()
						,"insert into wiki_pages (select ? as x where not exists " +
								"	(select * from wiki_pages wp where wp.page_name = ? ) )"
						, page, page);
				JDBCUtil.doUpdate(WikiFilter.dbConnection()
								,"insert into wiki_pages_versions values (?,?,?) "
								, page, team,version);
				result= Integer.valueOf(0);
			}
			else // wrong version number
				throw new WikiException("Version conflict");
		}
		else 
			result= Integer.valueOf(version.intValue()+1);
		JDBCUtil.doUpdate(WikiFilter.dbConnection(),
						  "insert into change_log (page_name,team_name,username,version,change_date)" +
						  	" values ( ?,?,?,?, current_timestamp )",
						  page,team,user,result);
		return result;
	}
	
	public static Integer versionForEdit(String page,String user,  String team) throws SQLException {
		if (team == null) team =preferredTeam;
		//TODO: check that the current user is allowed to do edits
		Integer version = JDBCUtil.maybeRetrieveOneInt(
				WikiFilter.dbConnection(),
				"select last_version from wiki_pages_versions where page_name= ? and team_name= ? ",
				page,team);
		if (version== null) version= Integer.valueOf(0);
		return  version;
	}


	/**
	 * 
	 * @param page
	 * @param user
	 * @param team
	 * @return an SExpr PAIR, the first element is the version, the next element is a list 
	 * of team_names for alternative views
	 * @throws SQLException
	 */
	public static SExpr findBestVersionMatch(String page, String user, String team)  throws SQLException {
		if (team == null) team =preferredTeam;
		Integer version = JDBCUtil.maybeRetrieveOneInt(
				WikiFilter.dbConnection(),
				"select last_version from wiki_pages_versions where page_name= ? and team_name= ? ",
				page,team);
		SExpr vExpr= version != null ? SExpr.make(version) : SExpr.make(0);
		ResultSet rs= JDBCUtil.query(WikiFilter.dbConnection(),
				"SELECT DISTINCT team_name FROM wiki_pages_versions WHERE page_name= ? and team_name <> ? ", page,team);
		SExpr.ListConstructor lc= new SExpr.ListConstructor();
		
		while  (rs.next()) {
			lc.append(SExpr.make(rs.getString(1)));
		}
		return vExpr.cons(lc.make());
	}


	/*public static Integer versionForRead(String page, String team, String user) {
		return new Integer(0);
	}*/
	
	public static boolean checkUsernameForLogin(String username) throws SQLException {
		if (username== null || username.length()==0) return true;
		Connection dbConn= WikiFilter.dbConnection();
		dbConn.setAutoCommit(false);
		Integer c= JDBCUtil.retrieveOneIntStrict(dbConn,"select count(*) from wiki_users where username=?",username);
		if (c.intValue() == 0) {
			int updateCount= JDBCUtil.doUpdate(dbConn,"insert into wiki_users(username) values (?)",username);
		}
		dbConn.commit();
		return true;
	}

	public static String anonymousUser(String remoteAddr) throws SQLException {
		// TODO check for username with this IP address
		Connection dbConn= WikiFilter.dbConnection();
		dbConn.setAutoCommit(false);
		Integer c= JDBCUtil.retrieveOneIntStrict(dbConn,"select count(*) from wiki_users where username=?",remoteAddr);
		if (c.intValue() == 0) {
			int updateCount= JDBCUtil.doUpdate(dbConn,"insert into wiki_users(username,is_anonymous) values (?,?)",remoteAddr,true);
		}
		dbConn.commit();
		return remoteAddr;
	}

	public static String normalize(String page) {
		// TODO: optimize so it's not creating GC stuff
		String result=page.trim().toLowerCase();
		//remove trailing _
		int i,j;
			for (i=0; result.charAt(i)=='_';i++);
			for (j=result.length(); result.charAt(j-1)=='_';j--);
		return result.substring(i,j);
	}

	public static SExpr teamMembership(String teamName) throws SQLException {
		if (teamName.equals("0"))
			return SExpr.make("<<everybody is a member of team 0. This team cannot be changed >>");
		ResultSet rs= JDBCUtil.query(WikiFilter.dbConnection()," select username from team_membership where team_name=? ", teamName);
		ListConstructor lc= new SExpr.ListConstructor();
		while (rs.next()) {lc.append(SExpr.make(rs.getString(1))); }
		return lc.make();
	}
	
	public static void processTeamUpdate(String user, String team, SExpr oldTeam, SExpr newTeam) throws SQLException {
		if (team.equals("0"))
			throw new WikiException("<<everybody is a member of team 0. This team cannot be changed >>");
		// ensure the editing user is part of the team
		Connection dbConn= WikiFilter.dbConnection(); 
		dbConn.setAutoCommit(false);
		//make sure the team exists
		JDBCUtil.doUpdate(WikiFilter.dbConnection()
				,"insert into wiki_teams (select ? as x, ? as y where not exists " +
						"	(select * from wiki_teams wt where wt.team_name = ? ) )"
				, team, user, team);
		newTeam= SExpr.make(user).cons(newTeam);
		SExpr diff= SExpressions.disjunction(oldTeam,newTeam);
		SExpr toDelete= diff.CAR(); 
		SExpr i=toDelete;
		if (! i.isNil()) {
			StringConstructor sc= new StringConstructor().append("( ?");
			ArrayList objValues= new ArrayList(); objValues.add(i.CAR().asString());
			//skip the first special
			i= i.CDR();
			while (! i.isNil()){
					sc.append(" , ?");
					objValues.add(i.CAR().asString());
					i= i.CDR();
			}
			sc.append(')');		
			
			JDBCUtil.doUpdate(dbConn,
								"delete from team_membership where username in "+sc.toString(), 
								objValues.toArray());
		}
		SExpr toInsert= diff.CDR();
		SExpr j= toInsert;
		while (!j.isNil()) {
			JDBCUtil.doUpdate(dbConn,
								"insert into team_membership values (?,?) "
								, team,j.CAR().asString());
			j= j.CDR();
		}
		dbConn.commit();
	}

	/**
	 * takes a title in the format with underscores
	 * and normalizes it by removing leading and trailing underscores 
	 */
	public static String normalizeUnderscoredTitle(String title){
		return title;
	}

	public static String normalizeUpperCaseTitle(String title) {
		return title;
	}

	/**
	 * retrieves of list of lists containing change record 
	 * to be matched as ( ?page_name ?team_name ?username ?version ?change_date ?change_comment)
	 * @param startTime
	 * @return
	 */
	public static SExpr changesSince(Calendar startTime) {
		try {
		return JDBCUtil.retrieveSExpr(
				WikiFilter.dbConnection(),
					"SELECT page_name, team_name, username, version, change_date, change_comment" +
					" FROM change_log WHERE change_date > ? ORDER BY change_date DESC ", startTime);
		} catch(SQLException ex) {
			throw new WikiException(ex);
		}
	}
}
