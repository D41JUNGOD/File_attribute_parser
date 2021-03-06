package file_manage;

import java.io.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class File_lock {

    private static final String algorithm = "AES";
    private static final String transformation = algorithm + "/ECB/PKCS5Padding";

    private void encrypt(String password, File source, File dest) throws Exception {
        Cipher cipher = Cipher.getInstance(transformation);
        SecretKeySpec key = new SecretKeySpec(password.getBytes(), algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);                                          //암호화 모드 설정
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new BufferedInputStream(new FileInputStream(source));
            output = new BufferedOutputStream(new FileOutputStream(dest));
            byte[] buffer = new byte[1024];
            int read = -1;
            output.write(password.getBytes());
            while ((read = input.read(buffer)) != -1) {
                output.write(cipher.update(buffer, 0, read));               //암호화
            }
            output.write(cipher.doFinal());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ie) {
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ie) {
                }
            }
            source.delete();
        }
}

    private int decrypt(String password, File source, File dest) throws Exception {
        String file_pw_str = "";
        int file_pw_int;
        Cipher cipher = Cipher.getInstance(transformation);
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new BufferedInputStream(new FileInputStream(source));
            BufferedReader reader = new BufferedReader(new FileReader(source));
            byte[] buffer = new byte[16];
            int read = -1;

            for(int i=1; i<=16; i++) {
                file_pw_int = reader.read();
                file_pw_str += (char)file_pw_int;
            }
            reader.close();
            if(!file_pw_str.equals(password))                                       //비밀번호 체크
                return 0;
            output = new BufferedOutputStream(new FileOutputStream(dest));
            SecretKeySpec key = new SecretKeySpec(password.getBytes(), algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key);                                  //복호화 설정

            int ct = 0;
            while ((read = input.read(buffer)) != -1) {
                if(ct != 0)
                    output.write(cipher.update(buffer, 0, read));       //복호화
                ct+=1;
            }
            output.write(cipher.doFinal());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ie) {
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ie) {
                }
            }
        }
        source.delete();
        return 1;
    }
    void encrypt_file(String password,String path) throws Exception{
        File_lock cipher = new File_lock();
        cipher.encrypt(password,new File(path),new File(path+".lock"));
    }

    int decrypt_file(String password,String path) throws Exception{
        File_lock cipher = new File_lock();
        if(cipher.decrypt(password,new File(path),new File(path.substring(0,path.length()-5))) == 0)
            return 0;
        return 1;
    }
}



