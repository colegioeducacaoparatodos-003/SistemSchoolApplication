package com.SistemSchool.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import jakarta.faces.context.FacesContext;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.servlet.ServletContext;

import org.primefaces.model.file.UploadedFile;

import java.sql.Connection;
import java.sql.PreparedStatement;

//import org.primefaces.model.file.UploadedFile;

public class Assistant implements Serializable {

	private static final long serialVersionUID = 1L;
	private String MSG_DADOS_SALVOS;

	public Assistant() {

	}

	public void coresCompleted(List<String> listaDeCores) throws Exception {
		Assistant acederAssistant = new Assistant();
		List<JsonValue> data = new ArrayList<JsonValue>();

		data = acederAssistant.descarregarJSON("jsonFiles", "colors.json", "color");
		for (int i = 0; i < data.size(); i++) {
			String str = data.get(i).toString();
			String[] arr = str.split("\"", 0);
			for (String a : arr) {
				if (!a.equals("")) {
					listaDeCores.add(a);
				}
			}
		}
	}

	public String converterDoubleString(double precoDouble) {

		DecimalFormat fmt = new DecimalFormat("#,##0.00"); // limita o numero de casas decimais
		String string = fmt.format(new BigDecimal(precoDouble));
		return string;

	}

	public boolean emailValidator(String email) {
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" + // part before @
				"(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

		Pattern pat = Pattern.compile(emailRegex);
		if (email == null)
			return false;
		return pat.matcher(email).matches();
	}

	public boolean tamanhoDaSenha(String senha) {
		if (senha.length() < 6)
			return true;
		return false;
	}

	public void tamanhoCompleted(List<String> listaDeCores, String tipo_do_produto) throws Exception {

		Assistant acederAssistant = new Assistant();
		List<JsonValue> data = new ArrayList<JsonValue>();

		if (tipo_do_produto.equals("Roupa"))
			data = acederAssistant.descarregarJSON("jsonFiles", "dressSize.json", "color");
		else if (tipo_do_produto.equals("Cal�ado"))
			data = acederAssistant.descarregarJSON("jsonFiles", "shoeSize.json", "color");
		else if (tipo_do_produto.equals("Acess�rio"))
			listaDeCores.add(" ");

		if (data.size() > 0) {
			for (int i = 0; i < data.size(); i++) {
				String str = data.get(i).toString();
				String[] arr = str.split("\"", 0);

				for (String a : arr)
					if (!a.equals(""))
						listaDeCores.add(a);
			}
		}
	}

	public void materialCompleted(List<String> listaDeCores, String tipo_do_produto) throws Exception {
		Assistant acederAssistant = new Assistant();
		List<JsonValue> data = new ArrayList<JsonValue>();

		if (tipo_do_produto.equals("Roupa"))
			data = acederAssistant.descarregarJSON("jsonFiles", "dressMaterial.json", "color");
		else if (tipo_do_produto.equals("Cal�ado"))
			data = acederAssistant.descarregarJSON("jsonFiles", "shoeMaterial.json", "color");
		else if (tipo_do_produto.equals("Acess�rio"))
			listaDeCores.add(" ");

		if (data.size() > 0) {
			for (int i = 0; i < data.size(); i++) {
				String str = data.get(i).toString();
				String[] arr = str.split("\"", 0);
				for (String a : arr) {
					if (!a.equals(""))
						listaDeCores.add(a);
				}

			}
		}
	}

	public List<String> coresCompleted(String query) throws Exception {
		List<String> resultados = new ArrayList<String>();

		Assistant acederAssistant = new Assistant();
		List<JsonValue> data = new ArrayList<JsonValue>();

		data = acederAssistant.descarregarJSON("jsonFiles", query, "color");
		for (int i = 0; i < data.size(); i++) {
			String str = data.get(i).toString();

			String[] arr = str.split("\"", 0);

			for (String a : arr) {
				if (!a.equals(""))
					resultados.add(a);
			}

		}
		return resultados;
	}

	@SuppressWarnings("unused")
	public JsonArray descarregarJSON(String caminho, String arquivo, String jsonItem) throws Exception {
		InputStream inputStream = null;
		FacesContext contex = FacesContext.getCurrentInstance();
		String path = contex.getExternalContext().getRealPath("/");
		System.out.println("Caminho Ler: " + path);
		File inputFile = new File(path + File.separator + caminho + File.separator + arquivo);

		inputStream = new FileInputStream(inputFile);

		JsonReader reader = (JsonReader) Json.createReader(inputStream);
		JsonObject dataObject = reader.readObject();
		dataObject.getJsonArray(jsonItem);
		List<JsonValue> data = new ArrayList<JsonValue>();

		data = dataObject.getJsonArray(jsonItem);

		reader.close();
		return dataObject.getJsonArray(jsonItem);
	}

	@SuppressWarnings("unused")
	public boolean eliminarFicheiro(Connection connection, String query, String nomeDoFicheiro,
			String nomeDoFicheiroNaBD, String nomeDaPasta) throws SQLException {

		PreparedStatement stmt = connection.prepareStatement(query);
		stmt.setString(1, nomeDoFicheiro);
		ResultSet rs = stmt.executeQuery();
		String ficheiro = null;
		while (rs.next())
			ficheiro = rs.getString(nomeDoFicheiroNaBD);

		FacesContext contex = FacesContext.getCurrentInstance();
		ServletContext servletContext = (ServletContext) contex.getExternalContext().getContext();
		String path = contex.getExternalContext().getRealPath("/");

		File ficheiroAEliminar = new File(path + File.separator + nomeDaPasta + File.separator + ficheiro);
		if (ficheiroAEliminar.delete())
			return true;
		return false;
	}

	@SuppressWarnings("unused")
	public boolean eliminarFicheiroUnico(String nomeDoFicheiro, String nomeDaPasta) throws SQLException {
		String ficheiro = null;
		boolean flag = false;
		int intFlag = 0;

		ficheiro = nomeDoFicheiro;
		FacesContext contex = FacesContext.getCurrentInstance();
		ServletContext servletContext = (ServletContext) contex.getExternalContext().getContext();
		String path = contex.getExternalContext().getRealPath("/");
		File ficheiroAEliminar = new File(path + File.separator + nomeDaPasta + File.separator + ficheiro);

		if (ficheiroAEliminar.delete()) {
			flag = true;
		}
		intFlag++;

		if (intFlag > 0)
			flag = true;
		return flag;
	}

	public String novoNome(String fileType) {
		String data = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
		String[] dataArray = null;
		dataArray = data.split("_", 0);
		String minutoESegundo = "";
		for (String a : dataArray)
			minutoESegundo += "" + a;
		dataArray = minutoESegundo.split(":", 0);
		data = "img";
		for (String a : dataArray)
			data += "-" + a;

		String[] fileTypeArray = fileType.split("/");
		int i = 0;
		for (String a : fileTypeArray) {
			if (i == 1)
				fileType = a;
			i++;
		}

		System.out.println("fileTypeArray2: " + fileType);

		System.out.println("Novonome3:" + data + "img." + fileType);
		return data + "img." + fileType;
	}

	public String retornarNomeCompleto(ResultSet rs) throws SQLException {
		while (rs.next())
			return rs.getString("primeiro_nome_da_pessoa") + " " + rs.getString("nome_do_meio_da_pessoa") + " "
					+ rs.getString("ultimo_nome_da_pessoa");
		return null;
	}

	public String retornarNomeDoArquivo(UploadedFile arquivo) throws IOException {
		String nomeDoFicheiro = "";
		if (!validarValorNulo("" + arquivo))
			nomeDoFicheiro = arquivo.getFileName();
		return nomeDoFicheiro;
	}

	public String retornarNovoNomeDoFicheiro() throws SQLException {
		String nome = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
		return nome;
	}

	public boolean validarValorNulo(String valor) {
		if (valor.equals("null"))
			return true;
		return false;
	}

	public String validarUserValorNulo(String valor) {
		if (("" + valor).equals("null"))
			return "0";
		return valor;
	}

	public boolean validarValorVazio(String valor) {
		if (valor.equals(""))
			return true;
		return false;
	}

	public boolean validarNumAngolano(String num) {
		char[] part = num.toCharArray();
		if (Integer.parseInt("" + part[5]) == 9) {

			if (Integer.parseInt("" + part[6]) > 0 && Integer.parseInt("" + part[6]) <= 5)
				return true;
			else if (Integer.parseInt("" + part[6]) == 9)
				return true;
		} else if (Integer.parseInt("" + part[5]) == 2)
			if (Integer.parseInt("" + part[6]) == 2)
				if (Integer.parseInt("" + part[7]) == 2)
					return true;
		return false;
	}

	public int getInteger(String string) {
		try {
			return Integer.valueOf(string);
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	public double getDouble(String string) {
		try {
			return Double.valueOf(string);
		} catch (NumberFormatException ex) {
			return 0.00;
		}
	}

	public int readNumeroRandomico(int min, int max) {
		Random random = new Random();
		return random.nextInt(max - min) + min;
	}

	public String getMSG_DADOS_SALVOS() {
		return MSG_DADOS_SALVOS;
	}

	public void setMSG_DADOS_SALVOS(String mSG_DADOS_SALVOS) {
		MSG_DADOS_SALVOS = mSG_DADOS_SALVOS;
	}

	public InputStream getFileFromResourceAsStream(String fileName) {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(fileName);

		if (inputStream == null) {
			throw new IllegalArgumentException("file not found" + fileName);
		} else {
			return inputStream;
		}
	}

	public File getFileFromResource(String fileName) throws URISyntaxException {
		ClassLoader classLoader = getClass().getClassLoader();
		URL resource = classLoader.getResource(fileName);
		if (resource == null) {
			throw new IllegalArgumentException("file no found" + fileName);
		} else {
			return new File(resource.toURI());
		}

	}

	public static void printInputStream(InputStream is) {
		try (InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
				BufferedReader reader = new BufferedReader(streamReader)) {
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
			LoggerUtil.logError("Erro to print Input: " + e.getMessage());
		}
	}

	public static void printFile(File file) {
		List<String> lines;
		try {
			lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
			lines.forEach(System.out::println);
		} catch (IOException e) {
			LoggerUtil.logError("Erro to print file : " + e.getMessage());
			e.printStackTrace();
		}
	}

}