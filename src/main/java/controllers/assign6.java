package controllers;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/")
public class assign6 extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setHeader("restApp-version", "1.0");
        PrintWriter outputHTML = response.getWriter();
        outputHTML.println("<html>");
        outputHTML.println(" <body>");
        outputHTML.println(" <h1>Welcome to assign6 by Arsh</h1>");
        outputHTML.println(" </body>");
        outputHTML.println("</html>");
        outputHTML.close();
    }
}