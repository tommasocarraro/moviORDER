/*
 * Servlet che si occupa di inviare un ordine, dati i seguenti dati:
 * path: path di connessione al db dell'azienda fornitrice dell'utente loggato;
 * codici: codici degli articoli checkati dall'utente per essere ordinati;
 * username: username dell'utente loggato;
 * codCliFor: codice del cliente;
 * desCliFor: nome del cliente;
 * codDoc: codice del documento da creare;
 * data: data del documento da creare;
 * note: note sul documento da creare;
 * codAz: codice dell'azienda fornitrice.
 * La pipeline di processamento di un ordine consiste in:
 * 1. Creazione di un record nella tabella DocTes che indichi che � stato creato un nuovo documento;
 * 2. Spostamento degli articoli checkati da TmpRig a DocRig per l'utente loggato;
 * 3. Invio di una mail di conferma all'utente che ha ordinato e in CC inserimento dell'indirizzo dell'azienda fornitrice.
 * N.B. Il codice si occupa anche di formattare una tabella HTML per visualizzare i dati degli articoli ordinati sulla e-mail.
 * La servlet ritorna un messaggio di fail se l'ordine non � andato a buon fine, oppure un messaggio di done se l'ordine � andato a buon fine.
 */
package servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dbConnection.DatabaseConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import utility.GetParam;
import utility.MailUtility;

/**
 * Servlet implementation class SendOrder
 */
@WebServlet("/SendOrder")
public class SendOrder extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SendOrder() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String connectionUrl = request.getParameter("path");
		String codici = request.getParameter("codici");
		String username = request.getParameter("username");
		String codCliFor = request.getParameter("codCliFor");
		String desCliFor = request.getParameter("desCliFor");
		String codDoc = request.getParameter("codDoc");
		String data = request.getParameter("data");
		String note = request.getParameter("note");
		String codAzienda = request.getParameter("codAz");
		String oggettoMail = "Registrato documento "+codDoc+" del "+data+" da "+desCliFor;
		String testoMail = "In data <strong>"+data+"</strong> l�utente <strong>"+username+"</strong> dall�azienda <strong>"+desCliFor+"</strong> ha inviato il seguente documento:<br/><br/>";
		testoMail += "<table style='border-collapse: collapse;'><tr><th style='border: 1px solid black;'>Quantit�</th><th style='border: 1px solid black;'>Articolo</th><th style='border: 1px solid black;'>Descrizione</th></tr>";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String json = "";
		Date parsed = null;
		try {
			parsed = format.parse(data);
		}catch(ParseException e) {
			e.printStackTrace();
		}
		java.sql.Date dat = new java.sql.Date(parsed.getTime());
		DatabaseConnection dbConnect = new DatabaseConnection(connectionUrl);
		try {
			dbConnect.connectToDB();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		//inserimento in DocTes della testata del documento
		int affectedRows = 0;
		affectedRows = dbConnect.doUpdateQuery("insert into DocTes(CodDoc,CodCliFor,DataDoc,Note) values ('"+codDoc+"','"+
				codCliFor+"','"+dat+"','"+note+"')");
		if(affectedRows == 0) {
			json = "{\"messaggio\":\"fail\"}";
		}
		ResultSet rs = dbConnect.doQuery("select max(Id_DocTes) from DocTes"); //mi prendo l'ultimo id inserito che sar� il massimo perch� sono auto-incrementati
		String idDocTes = "";
		try {
			while(rs.next()) {
				idDocTes = rs.getString(1);
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		String[] code = codici.split(",");
		//prelevo i dati da TmpRig, li inserisco in DocRig e poi li cancello
		String query = "select TmpRig.codArt,TmpRig.Quantita,TmpRig.Note,Art.DesArt from TmpRig inner join Art on TmpRig.codArt=Art.CodArt where TmpRig.username='"+username+"' and (";
		for(int i=0;i<code.length;i++) {
			query += "TmpRig.CodArt='"+code[i]+"' or ";
		}
		query = query.substring(0, query.length() - 4); //tolgo l'ultimo or di troppo
		query += ")";
		rs = null;
		rs = dbConnect.doQuery(query);
		String desc = "";
		try {
			while(rs.next()) {
				testoMail += "<tr>";
				affectedRows = dbConnect.doUpdateQuery("insert into DocRig(Id_DocTes,Username,CodArt,Quantita,Note) values('"+
							idDocTes+"','"+username+"','"+rs.getString(1)+"','"+rs.getInt(2)+"','"+rs.getString(3)+"')");
				if(affectedRows == 0) {
					json = "{\"messaggio\":\"fail\"}";
				}
				affectedRows = dbConnect.doUpdateQuery("delete from TmpRig where CodArt='"+rs.getString(1)+"' and Username='"+username+"'");
				if(affectedRows == 0) {
					json = "{\"messaggio\":\"fail\"}";
				}
				desc = rs.getString(4);
				desc = desc.replaceAll("\"", "''");
				testoMail += "<td  style='border: 1px solid black; text-align: center;'>"+rs.getInt(2)+"</td><td style='border: 1px solid black; text-align: center;'>"+rs.getString(1)+"</td><td style='border: 1px solid black; text-align: center;'>"+desc+"</td>";
				testoMail += "</tr>";
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		testoMail += "</table><br/>Mail automatica inviata da <strong>moviORDER</strong>, non rispondere a questo messaggio.";
		String mittente = "noreply@movidat.com";
		String utenteDest = "";
		String aziendaDest = "";
		String host = "";
		int port = 0;
		String user = "";
		String pwd = "";
		dbConnect.closeConnection();
		if(json != "{\"messaggio\":\"fail\"}") {
			json = "{\"messaggio\":\"done\"}";
			dbConnect = new DatabaseConnection(GetParam.getUrlCommonDB());
			try {
				dbConnect.connectToDB();
			}catch(SQLException e) {
				e.printStackTrace();
			}
			rs = null;
			rs = dbConnect.doQuery("select EmailU from Users where UserName='"+username+"'");
			try {
				while(rs.next()) {
					utenteDest = rs.getString(1);
				}
			}catch(SQLException e) {
				e.printStackTrace();
			}
			rs = null;
			rs = dbConnect.doQuery("select EmailA,Host,Port,Username,Password from Aziende where CodAzienda='"+codAzienda+"'");
			try {
				while(rs.next()) {
					aziendaDest = rs.getString(1);
					host = rs.getString(2);
					port = rs.getInt(3);
					user = rs.getString(4);
					pwd = rs.getString(5);
				}
			}catch(SQLException e) {
				e.printStackTrace();
			}
			MailUtility mail = new MailUtility(host,port,user,pwd);
			try{
		      mail.sendMail(utenteDest, aziendaDest, mittente, oggettoMail, testoMail);
		    }catch (MessagingException exc){
		      exc.printStackTrace();
		    }
			dbConnect.closeConnection();
			try {
				rs.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
		response.setContentType("application/json");
		response.getWriter().write(json);
	}

}
