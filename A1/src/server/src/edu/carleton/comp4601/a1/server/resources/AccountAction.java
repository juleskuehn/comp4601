package edu.carleton.comp4601.a1.server.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import edu.carleton.comp4601.a1.server.dao.AccountStore;
import edu.carleton.comp4601.a1.server.model.Account;

public class AccountAction {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	Integer id;
	AccountStore accounts;

	public AccountAction(UriInfo uriInfo, Request request, String id, AccountStore accounts) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.id = new Integer(id);
		this.accounts = accounts;
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Account getAccount() {
		Account a = accounts.find(id);
		if (a == null) {
			throw new RuntimeException("No such account: " + id);
		}
		return a;
	}
	
	@GET
	@Path("description")
	@Produces(MediaType.TEXT_PLAIN)
	public String getAccountDescription() {
		Account a = this.getAccount();
		return a.getDescription();
	}


	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	public Response putAccount(JAXBElement<Account> acct) {
		try {
			Account account = acct.getValue();
			if (account.balance() < 0) account.setBalance(0);
			if (accounts.find(account.getId()) != null)
				accounts.update(account);
			else
				accounts.open(account.getId(), account.getBalance(), account.getDescription());
			return Response.created(uriInfo.getAbsolutePath()).build();
		} catch (Exception e) {
			return Response.notModified(e.getMessage()).build();
		}
	}
	

	@DELETE
	public void deleteAccount() {
		if (!accounts.close(id))
			throw new RuntimeException("Account " + id + " not found");
	}
}
