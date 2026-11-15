package com.SistemSchool.modulo_secrtaria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.SistemSchool.config.SessionBean;
import com.SistemSchool.dto.UserDTO.UserResponseDTO;
import com.SistemSchool.modulo_Financeiro.model.Fee;
import com.SistemSchool.modulo_secrtaria.model.Pagamento;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

class PdfGeneratorServiceTest {

    @Test
    void generatesOneReceiptWithEverySelectedFeeAndCombinedTotal() throws IOException {
        PdfGeneratorService service = new PdfGeneratorService();
        Pagamento first = pagamento("DOC-1", "Propina de Janeiro", "1000");
        Pagamento second = pagamento("DOC-2", "Taxa de Matrícula", "2500");
        UserResponseDTO user = new UserResponseDTO();
        user.setPkUser(1);
        user.setEmail("teste@example.com");
        user.setPersonName("Paulino Goncalves");

        byte[] pdf;
        try (MockedStatic<SessionBean> sessionBean = org.mockito.Mockito.mockStatic(SessionBean.class)) {
            sessionBean.when(SessionBean::getLoggedUserForCurrentRequest).thenReturn(user);
            pdf = service.generatePagamentosPdf(List.of(first, second));
        }

        PdfReader reader = new PdfReader(pdf);
        try {
            assertEquals(1, reader.getNumberOfPages());
            assertTrue(reader.getPageSize(1).getWidth() < PageSize.A5.getWidth());
            String text = new PdfTextExtractor(reader).getTextFromPage(1);
            assertTrue(text.contains("Propina de Janeiro"));
            assertTrue(text.contains("Taxa de Matrícula"));
            assertTrue(text.contains("Emitido por: Paulino Goncalves (ID 1)"));
            assertTrue(text.contains("Emitido em: "));
            assertFalse(text.contains("teste@example.com"));
            String totalText = text.substring(text.indexOf("TOTAL RECEBIDO"));
            assertTrue(totalText.matches("(?s).*3\\D*500[,.]00\\s*Kz.*"), totalText);
        } finally {
            reader.close();
        }
    }

    private Pagamento pagamento(String numeroDocumento, String descricao, String valor) {
        Fee fee = new Fee();
        fee.setDescription(descricao);

        Pagamento pagamento = new Pagamento();
        pagamento.setNumeroDocumento(numeroDocumento);
        pagamento.setFee(fee);
        pagamento.setValor(new BigDecimal(valor));
        pagamento.setTotal(new BigDecimal(valor));
        return pagamento;
    }
}
