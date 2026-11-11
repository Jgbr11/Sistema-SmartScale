package br.com.smartscale.dbfirst;

import java.util.List;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

@Component("rotulo")
public class Rotulo {

    private static final List<String> CAMPOS_DE_NOME = List.of("nomeGuerra", "nome", "sigla", "titulo", "descricao", "login", "data");

    public String de(Object registro) {
        if (registro == null) {
            return "";
        }
        BeanWrapper bean = new BeanWrapperImpl(registro);
        for (String campo : CAMPOS_DE_NOME) {
            if (bean.isReadableProperty(campo) && bean.getPropertyValue(campo) != null) {
                return bean.getPropertyValue(campo).toString();
            }
        }
        return registro.getClass().getSimpleName();
    }
}
