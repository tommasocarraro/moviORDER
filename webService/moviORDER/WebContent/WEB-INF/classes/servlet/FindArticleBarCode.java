/*
 * Servlet che rappresenta il servizio di ricerca del codice articolo corrispondente a un codice a barre passato.
 * Prende in input il codice scansionato con la camera del cellulare e la path per collegarsi al db dell'azienda dove � contenuta la tabella Art.
 * Cerca il CodArt corrispondente al codice a barre passato e restituisce un JSON contente il CodArt corrispondente oppure un messaggio d'errore se non viene trovato.
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

/**
 * Servlet implementation class FindArticleBarCode
 */
@WebServlet("/FindArticleBarCode")
public class FindArticleBarCode extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FindArticleBarCode() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String barCode = request.getParameter("codArtAlias");
		String path = request.getParameter("path");
		DatabaseConnection dbConnect = new DatabaseConnection(path);
		try {
			dbConnect.connectToDB();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		boolean trovato = false;
		String json = "";
		ResultSet rs = dbConnect.doQuery("select CodArt from ArtAlias where Alias='"+barCode+"'");
		try {
			while(rs.next()) {
				trovato = true;
				json = "{\"codArt\":\""+rs.getString(1)+"\",\"messaggio\":\"yes\"}";
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		if(trovato == false) {
			//il codice non esiste
			json = "{\"messaggio\":\"no\"}";
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
