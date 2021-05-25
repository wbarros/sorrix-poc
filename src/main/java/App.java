package com.sorrix.poc;

import java.io.*;
import java.rmi.RemoteException;

import br.com.bradesco.webta.clientepj.webservices.WSWEBTAProxy;
import br.com.bradesco.webta.clientepj.webservices.beans.WSRetornoTO;
import br.com.bradesco.webta.clientepj.webservices.faults.WSWEBTAFault;
import br.com.bradesco.webta.security.crypto.WEBTACryptoUtil;
import br.com.bradesco.webta.security.exception.CryptoException;
import br.com.bradesco.webta.security.exception.ParameterException;
import com.sorrix.poc.util.FileUtils;
import br.com.bradesco.webta.clientepj.webservices.beans.WSSessaoTO;

public class App {

    /**
     * Tamanho do campo Logical Length (LL)
     */
    protected static final int TAMANHO_LL = 4;
    /**
     * Tamanho maximo do LL
     */
    protected static final int LL_MAX = 524288;
    /**
     * Erro de leitura
     */
    protected static final int ERRO_LEITURA = -1;
//    public static final String ARQUIVO_CRIPTOGRAFIA = "config/criptografia202008311142.bin";
//    public static final String ARQUIVO_TRANSFERENCIA = "config/transferencia202008311142.bin";
    public static final String ARQUIVO_CRIPTOGRAFIA = "config/WebServiceIbpj202104201049/criptografia202104201049.bin";
    public static final String ARQUIVO_TRANSFERENCIA = "config/WebServiceIbpj202104201049/transferencia202104201049.bin";

    public static void main(String[] args) {
        try {
            // Chave para descriptografar o conteudo dos arquivos de retorno
            byte[] fileKeyCripto = WEBTACryptoUtil.decodeKeyFile(FileUtils.getFileFromResource(ARQUIVO_CRIPTOGRAFIA), "102030");

            //contém a chave e o ID necessários para realizar a transmissão/recepção automática de arquivos
            byte[] transfFileKey = WEBTACryptoUtil.decodeKeyFile(FileUtils.getFileFromResource(ARQUIVO_TRANSFERENCIA), "102030");

            //Obtem id do cliente
            String idClienteTransAutom = WEBTACryptoUtil.getIdFromDecodedFileKey(transfFileKey);
            //Obtem chave de criptografia do desafio
            byte[] transfKey = WEBTACryptoUtil.getKeyFromDecodedFileKey(transfFileKey);

            getAndWriteFile(idClienteTransAutom, transfKey);

        } catch (CryptoException | IOException | ParameterException ce) {
            System.out.println("Erro ao decodificar arquivo " + ce);
        }
    }

    public static void getAndWriteFile(String idCliente, byte[] transfKey) {
        WSWEBTAProxy ws = new WSWEBTAProxy();
        WSSessaoTO wssessaoto = null;
        long offSet = 0;
        ws.setEndpoint("https://www.webtatransferenciadearquivos.bradesco.com.br/webta/services/WSWEBTA");
        //ABRE SESSAO
        try {
            wssessaoto = ws.criarSessao(idCliente);
            //Criptografa desafio recebido na abertura de sessao
            byte[] desafioCripto = WEBTACryptoUtil.encode(wssessaoto
                    .getDesafio().getBytes(), transfKey);
            //ENVIA DESAFIO CRIPTOGRAFADO PARA O SERVIDOR
            ws.habilitarSessao(wssessaoto.getCTRL(), desafioCripto);

            /* -------------------------
            RECEBE UM ARQUIVO DE RETORNO
            --------------------------*/
            FileOutputStream fos = null;
            //Obtem primeiro bloco do arquivo
            WSRetornoTO ret = ws.obterBlocoRetorno(wssessaoto.getCTRL(), 0, 0,
                    8192);
            //Verifica se retornou arquivo para recepcao
            if (ret.getNomeLogicoArquivo() != null) {
                fos = new FileOutputStream( "config/" +ret.getNomeLogicoArquivo());
                //Monta LL da informacao
                int tamBuffer = ret.getConteudo().length;
                byte[] ll = new byte[TAMANHO_LL];
                for (int i = 0; i < TAMANHO_LL; i++) {
                    int offset = (ll.length - 1 - i) * 8;
                    ll[i] = (byte) ((tamBuffer >>> offset) & 0xFF);
                }
                //Grava LL
                fos.write(ll);
                //Grava dados
                fos.write(ret.getConteudo());

                offSet = ret.getQuantidadeBytesLidos();
                long totalSize = ret.getQuantidadeBytesArquivo();
                //Loop para receber demais blocos do arquivo
                while (offSet < totalSize) {
                    ret = ws.obterBlocoRetorno(wssessaoto.getCTRL(), ret.getNumeroArquivo(), offSet, 8192);
                    tamBuffer = ret.getConteudo().length;
                    //Monta LL
                    for (int i = 0; i < TAMANHO_LL; i++) {
                        int offset = (TAMANHO_LL - 1 - i) * 8;
                        ll[i] = (byte) ((tamBuffer >>> offset) & 0xFF);
                    }
                    //Grava LL e dados
                    fos.write(ll);
                    fos.write(ret.getConteudo());
                    offSet += ret.getQuantidadeBytesLidos();
                }
                if (fos != null)
                    fos.close();
            } else {
                System.out.println("Nenhum arquivo de retorno encontrado para "+ idCliente);
            }

        } catch (WSWEBTAFault e) {
            System.out.println("Erro na execucao de metodo do Web Service - Codigo de erro: " +
                    e.getCodigo() + " - Mensagem descritiva: " + e.getMessage1());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(wssessaoto != null)
                    ws.encerrarSessao(wssessaoto.getCTRL());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

}
