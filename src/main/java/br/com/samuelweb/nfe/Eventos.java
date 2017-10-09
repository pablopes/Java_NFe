package br.com.samuelweb.nfe;

import br.com.samuelweb.nfe.dom.ConfiguracoesIniciaisNfe;
import br.com.samuelweb.nfe.exception.NfeException;
import br.com.samuelweb.nfe.exception.NfeValidacaoException;
import br.com.samuelweb.nfe.util.ConstantesUtil;
import br.com.samuelweb.nfe.util.ObjetoUtil;
import br.com.samuelweb.nfe.util.WebServiceUtil;
import br.inf.portalfiscal.nfe_4.wsdl.NFeRecepcaoEvento4Stub;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;

class Eventos {

    static String enviarEvento(String xml, String tipoEvento, boolean valida, String tipo) throws NfeException {

        try {

            xml = Assinar.assinaNfe(xml, Assinar.EVENTO);

            if(valida){
                String erros ="";
                switch (tipoEvento) {
                    case ConstantesUtil.EVENTO.CANCELAR:
                        erros = Validar.validaXml(xml, Validar.CANCELAR);
                        break;
                    case ConstantesUtil.EVENTO.CCE:
                        erros = Validar.validaXml(xml, Validar.CCE);
                        break;
                    case ConstantesUtil.EVENTO.MANIFESTACAO:
                        erros = Validar.validaXml(xml, Validar.MANIFESTAR);
                        break;
                    default:
                        break;
                }

                if(!ObjetoUtil.isEmpty(erros)){
                    throw new NfeValidacaoException("Erro Na Validação do Xml: "+erros);
                }
            }

            if (ConfiguracoesIniciaisNfe.getInstance().isLog()) {
                System.out.println("Xml Evento: " + xml);
            }

            OMElement ome = AXIOMUtil.stringToOM(xml);

            NFeRecepcaoEvento4Stub.NfeDadosMsg dadosMsg = new NFeRecepcaoEvento4Stub.NfeDadosMsg();
            dadosMsg.setExtraElement(ome);

            String url ;
            if(tipoEvento.equals(ConstantesUtil.EVENTO.MANIFESTACAO)){
                url =  WebServiceUtil.getUrl(ConstantesUtil.NFE, ConstantesUtil.SERVICOS.MANIFESTACAO);
            }else{
                url = WebServiceUtil.getUrl(tipo, ConstantesUtil.SERVICOS.EVENTO);
            }

            NFeRecepcaoEvento4Stub stub = new NFeRecepcaoEvento4Stub(url);
            NFeRecepcaoEvento4Stub.NfeResultMsg result = stub.nfeRecepcaoEvento(dadosMsg);

            return result.getExtraElement().toString();
        } catch (RemoteException | XMLStreamException e) {
            throw new NfeException(e.getMessage());
        }

    }

}