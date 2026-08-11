package com.SistemSchool.config;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.util.Locale;

@Named
@SessionScoped
public class LanguageController {

	private String language;
	private Locale locale;

	@PostConstruct
	public void init() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
			this.locale = context.getExternalContext().getRequestLocale();
		} else {
			this.locale = Locale.getDefault();
		}
	}

	public void actionIngles() {
		setLanguage("en");
	}

    public void actionEspanhol() {
        setLanguage("es");
    }

	public void actionPortugues() {
		setLanguage("pt");
	}

	public String getLanguage() {
		return language != null ? language : locale.getLanguage();
	}

	public void setLanguage(String language) {
		this.language = language;
		this.locale = new Locale(language);
		FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
	}

	public Locale getLocale() {
		if (locale == null) {
			FacesContext context = FacesContext.getCurrentInstance();
			locale = (context != null) ? context.getExternalContext().getRequestLocale() : Locale.getDefault();
		}
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

}