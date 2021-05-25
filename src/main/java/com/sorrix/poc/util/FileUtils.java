package com.sorrix.poc.util;

import java.io.*;

public class FileUtils {

    public static File getFileFromResource(String fileName) {
        return new File(fileName);
    }

    public static void printFile(String filePath) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(filePath);
            InputStreamReader reader = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(reader);
            String linha = br.readLine();
            while(linha != null) {
                System.out.println(linha);
                linha = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}