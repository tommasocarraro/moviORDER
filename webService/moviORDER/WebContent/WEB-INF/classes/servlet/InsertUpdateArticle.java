/*
 * Servlet che rappresenta il servizio di inserimento o modifica di un articolo in carrello.
 * Richiede la path di connessione al db dell'azienda di cui l'utente loggato � cliente e la query di insert o update dell'articolo.
 * La Servlet non fa altro che eseguire la query passata sul database passato.
 * La servlet ritorna un messaggio di fail se la modifica/inserimento non � andata a buon fine, oppure un messaggio di done se la modifica/inserimento � andata a buon fine.
 */
package servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dbConnection.DatabaseConnection;

/**
 * Servlet implementation class InsertUpdateArticle
 */
@WebServlet("/InsertUpdateArticle")
public class InsertUpdateArticle extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InsertUpdateArticle() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = request.getParameter("path");
		String query = request.getParameter("query");
		String json = "";
		DatabaseConnection dbConnect=new DatabaseConnection(url);
		try {
			dbConnect.connectToDB();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		int affectedRows = dbConnect.doUpdateQuery(query);
		if(affectedRows == 0) {
			json = "{\"messaggio\":\"fail\"}";
		}else {
			json = "{\"messaggio\":\"done\"}";
		}
		dbConnect.closeConnection();
		response.setContentType("application/json");
		response.getWriter().write(json);
	}

}
