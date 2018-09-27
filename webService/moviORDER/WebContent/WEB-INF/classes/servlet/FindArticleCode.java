/*
 * Servlet che esegue la ricerca di un codice articolo dato il codice stesso (scritto dall'utente su un input type text), la path di connessione al db dell'azienda dove � contenuta la tabella Art e lo username dell'utente che ha accesso all'applicazione.
 * La Servlet esegue una prima ricerca per verificare se il codice esiste nella tabella Art, infatti l'utente potrebbe aver digitato il codice sbagliato.
 * La Servlet esegue una seconda ricerca per verificare se il codice dell'articolo non � gi� presente nella tabella TmpRig, ovvero sell'utente non ha gi� l'articolo in carrello. In caso affermativo l'app visualizzer� un messaggio di notifica, in caso negativo verr� aperta la pagina per l'inserimento del nuovo prodotto in carrello.
 */
package servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dbConnection.DatabaseConnection;

/**
 * Servlet implementation class FindArticleCode
 */
@WebServlet("/FindArticleCode")
public class FindArticleCode extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FindArticleCode() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String article = request.getParameter("codArt");
		String connectUrl = request.getParameter("path");
		String username = request.getParameter("username");
		String tmp = "";
		DatabaseConnection dbConnect=new DatabaseConnection(connectUrl);
		try {
			dbConnect.connectToDB();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		String json = "";
		ResultSet rs = dbConnect.doQuery("select count(*) from Art where CodArt='"+article+"'");
		try {
			while(rs.next()) {
				if(rs.getInt(1)!=0) {
					rs.close();
					rs = dbConnect.doQuery("select count(*) from TmpRig where CodArt='"+article+"' and username='"+username+"'");
					while(rs.next()) {
						if(rs.getInt(1)!=0) {
							json = "{\"messaggio\":\"presente\",\"codice\":\""+article+"\"}";
						}else {
							json="{\"messaggio\":\"OK\",\"codice\":\""+article+"\"}";
						}
					}
				}else {
					//se non � andata a buon fine la ricerca del codice articolo cerco il codice a barre
					rs.close();
					rs = dbConnect.doQuery("select CodArt from ArtAlias where Alias='"+article+"'");
					while(rs.next()) {
						tmp = rs.getString(1);
						if(tmp != "") {
							rs.close();
							rs = dbConnect.doQuery("select count(*) from TmpRig where CodArt='"+tmp+"' and username='"+username+"'");
							while(rs.next()) {
								if(rs.getInt(1)!=0) {
									json = "{\"messaggio\":\"presente\",\"codice\":\""+tmp+"\"}";
								}else {
									json="{\"messaggio\":\"OK\",\"codice\":\""+tmp+"\"}";
								}
							}
						}else {
							json="{\"messaggio\":\"NO\",\"codice\":\"null\"}";
						}
					}
				}
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		response.setContentType("application/json");
		response.getWriter().write(json);
		dbConnect.closeConnection();
		try {
			rs.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}

}
