package com.SistemSchool.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.io.InputStream;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.primefaces.model.file.UploadedFile;

//Manage files	
public class FileImage {

	public FileImage() {
	}

	/*
	 * She tries to delete a specific file (if it exists) in a folder of the
	 * web application, and returns:true: if the file was successfully deleted or
	 * if it didn't exist at all.false: if the file
	 * was successfully deleted or if it didn't exist at all.false:
	 * if the file existed but could not be deleted (for example, due to lack of
	 * permissions).
	 */
	@SuppressWarnings("unused")
	public boolean eliminarFicheiro(String nomeDoFicheiro, String nomeDaPasta) throws SQLException {
		FacesContext contex = FacesContext.getCurrentInstance();
		ServletContext servletContext = (ServletContext) contex.getExternalContext().getContext();
		String path = contex.getExternalContext().getRealPath("/");

		File ficheiroAEliminar = new File(path + File.separator + nomeDaPasta + File.separator + nomeDoFicheiro);
		if (ficheiroAEliminar.exists()) {
			if (ficheiroAEliminar.delete())
				return true;
			else
				return false;
		}
		return true;
	}
	/*
	 * This function Save File Without Changing The Name aims to save (store) an
	 * uploaded file on the server,
	 * keeping the name provided by the parameter newFileName,
	 * without altering the content of the uploaded file.
	 */

	@SuppressWarnings("unused")
	public void salvarArquivoSemMudarONome(UploadedFile arquivo, String caminho, String novoNomeDoArquivo)
			throws IOException {
		OutputStream outputStream = null;
		FacesContext contex = FacesContext.getCurrentInstance();
		ServletContext servletContext = (ServletContext) contex.getExternalContext().getContext();
		String path = contex.getExternalContext().getRealPath("/");
		File outputFile = new File(path + File.separator + caminho + File.separator + novoNomeDoArquivo);
		outputStream = new FileOutputStream(outputFile);
		outputStream.write(arquivo.getContent());
		outputStream.close();
	}

	/*
	 * This method saves an uploaded file (UploadedFile) to the server,
	 * and then renames it to a new file name (novoNomeDoArquivo).
	 */
	@SuppressWarnings("unused")
	public void salvarArquivo(UploadedFile arquivo, String caminho, String novoNomeDoArquivo) throws IOException {
		OutputStream outputStream = null;
		FacesContext contex = FacesContext.getCurrentInstance();
		ServletContext servletContext = (ServletContext) contex.getExternalContext().getContext();
		String path = contex.getExternalContext().getRealPath("/");
		System.out.println("Caminho: " + path);
		File outputFile = new File(path + File.separator + caminho + File.separator + arquivo.getFileName());
		File novoNome = new File(path + File.separator + caminho + File.separator + "" + novoNomeDoArquivo);
		outputStream = new FileOutputStream(outputFile);
		outputStream.write(arquivo.getContent());
		outputStream.close();

		if (outputFile.renameTo(novoNome))
			System.out.println("renomeo" + novoNomeDoArquivo);
		else
			System.out.println("n renomeo" + novoNomeDoArquivo);

	}

	// This method is supposed to read a file from disk.
	public void lerArquivo(String arquivo, String caminho) throws IOException {
		InputStream inputStream = null;
		FacesContext contex = FacesContext.getCurrentInstance();
		String path = contex.getExternalContext().getRealPath("/");
		File inputFile = new File(path + File.separator + caminho + File.separator + arquivo);
		inputStream = new FileInputStream(inputFile);
		inputStream.read(arquivo.getBytes());
		inputStream.close();
	}

	// These methods are used to download PDF files from the server to the client
	// (browser)
	@SuppressWarnings({ "unused", "resource" })
	public void baixarFicheiro() throws IOException {

		final FacesContext fc = FacesContext.getCurrentInstance();
		final ExternalContext externalContext = fc.getExternalContext();

		ServletContext servletContext = (ServletContext) fc.getExternalContext().getContext();

		String path = fc.getExternalContext().getRealPath("/");

		final File file = new File(path + File.separator + "report" + File.separator + "reciboAlmirante");

		externalContext.responseReset();
		externalContext.setResponseContentType("application/pdf"); // ContentType.APPLICATION_OCTET_STREAM.getMimeType()
		externalContext.setResponseContentLength(Long.valueOf(file.lastModified()).intValue());
		externalContext.setResponseHeader("Content-Disposition", "attachment;filename=" + file.getName());

		final HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

		FileInputStream input = new FileInputStream(file);
		byte[] buffer = new byte[1024];
		final ServletOutputStream out = response.getOutputStream();

		while ((input.read(buffer)) != -1) {
			out.write(buffer);
		}

		out.flush();
		fc.responseComplete();

	}

	// These methods are used to download PDF files from the server to the client
	// (browser).
	@SuppressWarnings("resource")
	public void baixarFicheiro1() throws IOException {

		final FacesContext fc = FacesContext.getCurrentInstance();
		final ExternalContext externalContext = fc.getExternalContext();

		String path = fc.getExternalContext().getRealPath("/");

		final File file = new File(path + File.separator + "report" + File.separator + "Criatividade.pdf");

		externalContext.responseReset();
		externalContext.setResponseContentType("application/pdf"); // ContentType.APPLICATION_OCTET_STREAM.getMimeType()
		externalContext.setResponseContentLength(Long.valueOf(file.lastModified()).intValue());
		externalContext.setResponseHeader("Content-Disposition", "attachment;filename=" + file.getName());

		final HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

		FileInputStream input = new FileInputStream(file);

		byte[] buffer = new byte[1024];
		final ServletOutputStream out = response.getOutputStream();
		while ((input.read(buffer)) != -1) {
			out.write(buffer);
		}
		out.flush();
		fc.responseComplete();
	}
}