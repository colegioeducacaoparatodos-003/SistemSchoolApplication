package com.SistemSchool.util;

import com.SistemSchool.io.Perfil;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@FacesConverter("perfilConverter")
public class PerfilConverter implements Converter<Perfil> {

    @Override
    public Perfil getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Perfil.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Perfil value) {
        if (value == null) {
            return "";
        }
        return value.name();
    }
}