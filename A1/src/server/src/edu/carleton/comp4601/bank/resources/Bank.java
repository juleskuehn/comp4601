package edu.carleton.comp4601.bank.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import edu.carleton.comp4601.bank.dao.AccountStore;
import edu.carleton.comp4601.bank.dao.Accounts;
import edu.carleton.comp4601.bank.dao.AccountsMongoDB;
import edu.carleton.comp4601.bank.dao.UserStore;
import edu.carleton.comp4601.bank.dao.Users;
import edu.carleton.comp4601.bank.dao.UsersMongoDB;
import edu.carleton.comp4601.bank.dao.AccountsJAXB;
import edu.carleton.comp4601.bank.model.Account;
import edu.carleton.comp4601.bank.model.User;

/**
 * This is a simple set of RESTful APIs designed to simulate a 
 * bank with numbered bank accounts. No security is provided. It
 * is designed to show off Jersey 2.X functionality. Serialization
 * of accounts is supported. The BankClient can be used to talk to the 
 * bank as can a standard browser. 
 * 
 * @author tonywhite
 *
 */

@Path("/bank")
public class Bank {
	// Allows to insert contextual objects into the class,
	// e.g. ServletContext, Request, Response, UriInfo
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	
	public static AccountStore accounts;
	
	public static String ACCOUNTS_ADAPTER = "MEMORY";
	public static String USERS_ADAPTER = "MONGO";
	
	public static UserStore users;

	private String name;

	public Bank() {
		name = "The RESTful Bank";
		switch(ACCOUNTS_ADAPTER) {
		case "MEMORY":
			accounts = Accounts.getInstance();
			break;
		case "MONGO":
			accounts = AccountsMongoDB.getInstance();
			break;
		case "JAXB":
			accounts = AccountsJAXB.getInstance();
			break;
		}
		
		switch(USERS_ADAPTER) {
		case "MEMORY":
			users = Users.getInstance();
			break;
		case "MONGO":
			users = UsersMongoDB.getInstance();
			break;
		}
	}

	@GET
	public String printName() {
		return name;
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	public String sayXML() {
		return "<?xml version=\"1.0\"?>" + "<bank> " + name + " </bank>";
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHtml() {
		return "<html> " + "<title>" + name + "</title>" + "<body><h1>" + name
				+ "</body></h1>" + "</html> ";
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String sayJSON() {
		return "{\"" + name + "\"}";
	}

	@GET
	@Path("count")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCount() {
		long count = accounts.size();
		return String.valueOf(count);
	}

	@GET
	@Path("accounts")
	@Produces(MediaType.TEXT_XML)
	public List<Account> getAccounts() {
		List<Account> loa = new ArrayList<Account>();
		loa.addAll(accounts.getModel().values());
		return loa;
	}

	@POST
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void newAccount(@FormParam("id") String id,
			@FormParam("balance") String balance,
			@FormParam("description") String description,
			@Context HttpServletResponse servletResponse) throws IOException {

		String newDescription = description;
		if (newDescription == null)
			newDescription = "";

		int newId = new Integer(id).intValue();
		int newBalance = new Integer(balance).intValue();
		accounts.open(newId, newBalance, newDescription);

		servletResponse.sendRedirect("../create_account.html");
	}

	@Path("{acct}")
	public AccountAction getAccount(@PathParam("acct") String id) {
		return new AccountAction(uriInfo, request, id, accounts);
	}
	
	@Path("users")
	public UsersAction getUsers() {
		return new UsersAction(uriInfo, request, users);
	}
	
	
	// Users
	
	public class UsersAction {
		@Context
		UriInfo uriInfo;
		@Context
		Request request;
		
		Integer id;
		UserStore users;
		
		public UsersAction(UriInfo uriInfo, Request request, UserStore users) {
			this.uriInfo = uriInfo;
			this.request = request;
			this.users = users;
		}
		
		@Path("{user}")
		public UserAction getUser(@PathParam("user") String id) {
			return new UserAction(uriInfo, request, id, users);
		}
	}
	
	public class UserAction {
		@Context
		UriInfo uriInfo;
		@Context
		Request request;
		
		Integer id;
		UserStore users;
		
		public UserAction(UriInfo uriInfo, Request request, String id, UserStore users) {
			this.uriInfo = uriInfo;
			this.request = request;
			this.id = new Integer(id);
			this.users = users;
		}
		
		@GET
		@Produces(MediaType.APPLICATION_XML)
		public User getUser() {
			User user = users.find(id);
			if (user == null) {
				throw new RuntimeException("No such user: " + id);
			}
			return user;
		}

		@PUT
		@Consumes(MediaType.APPLICATION_XML)
		public Response putUser(JAXBElement<User> usr) {
			try {
				User user = usr.getValue();
				if (users.find(user.getId()) != null)
					users.update(user);
				else
					users.create(user.getId(), user.getName());
				return Response.created(uriInfo.getAbsolutePath()).build();
			} catch (Exception e) {
				return Response.notModified(e.getMessage()).build();
			}
		}
		
		@DELETE
		public void deleteUser() {
			if (!users.delete(id))
				throw new RuntimeException("User " + id + " not found");
		}
	}
}
