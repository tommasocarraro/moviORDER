/*
 * Servlet che rappresenta un servizio di autenticazione. L'applicazione invia una richiesta POST
 * alla Servlet sull'indirizzo /AuthenticationServlet.
 * Avviene la connessione alla tabella Users sul CommonDB e li avviene la ricerca della coppia username/password.
 * Esistono 4 casistiche:
 * nome utente non corretto;
 * password non corretta;
 * utente bloccato: se sul db il flag Bloccato == True;
 * utente/password corretti e restituzione di un json contenente il codice dell'azienda fornitrice dell'utente.
 * Questo codice verr� utilizzato successivamente per collegarsi al db dell'azienda sul server cloud oppure su un server dell'azienda stessa.
 */
package servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dbConnection.DatabaseConnection;
import java.sql.*;
import utility.GetParam;

/**
 * Servlet implementation class AuthenticationServlet
 */
@WebServlet("/AuthenticationServlet")
public class AuthenticationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AuthenticationServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		DatabaseConnection dbConnect=new DatabaseConnection(GetParam.getUrlCommonDB());
		try {
			dbConnect.connectToDB();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		ResultSet rs=dbConnect.doQuery("select * from Users");
		String json ="";
		json = generateResponse(rs,username,password);
		response.setContentType("application/json");
		response.getWriter().write(json);
		dbConnect.closeConnection();
		try {
			rs.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public String generateResponse(ResultSet rs, String user, String psw) {
		String json="";
		boolean trovato=false;
		try {
			while(rs.next() && trovato==false) {
				if(rs.getString(1).equals(user)) {
					trovato=true; //serve perch� cos� mi fermo al primo utente matchato
					if(rs.getString(2).equals(psw)) {
						if(rs.getBoolean(5)==false) {
							json="{\"messaggio\":\"OK\",\"codAz\":\""+rs.getString(3)+"\",\"username\":\""+
									rs.getString(1)+"\"}";
						}else {
							json="{\"messaggio\":\"L'utente "+user+" � stato bloccato!\"}";
						}
					}else {
						json="{\"messaggio\":\"La password inserita non � corretta!\"}";
					}
				}else {
					json="{\"messaggio\":\"Nome utente inesistente!\"}";
				}
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return json;
	}

}
