package com.softmotions.ncms.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Утилитный класс облегчающий создание и распаковку zip/jar архивов
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 * @version $Id: ZipUtils.java 17713 2011-09-26 10:06:20Z adam $
 */
public class ZipUtils {

    private ZipUtils() {
    }

    /**
     * Зипует указанный file результирующий архив кладется в outFile
     *
     * @param file
     * @param outFile
     * @throws IOException
     */
    public static void zipFile(File file, File outFile) throws IOException {
        zipFiles(new File[]{file}, outFile.toString());
    }

    /**
     * Зипует указанный file результирующий архив кладется в outFile, если флаг plain
     * установлен в true то в результирующий zip архив не будет скопирована структура каталогов
     * которая образует путь до файла (file)
     *
     * @param file
     * @param outFile
     * @param plain
     * @throws IOException
     */
    public static void zipFile(File file, File outFile, boolean plain) throws IOException {
        zipFiles(new File[]{file}, outFile.toString(), plain, false, null);
    }

    /**
     * Зипует указанное множество фалов files в zip архив под именем outFileName
     *
     * @param files
     * @param outFileName
     * @throws IOException
     */
    public static void zipFiles(File[] files, String outFileName) throws IOException {
        zipFiles(files, outFileName, false, false, null);
    }

    /**
     * Зипует исходное множество файлов files в архив с именем outFileName
     * если флаг plain установлен в true то в результирующий zip архив не будет скопирована структура каталогов
     * которая образует путь до файла (file), флагом jar указывается нужно ли создавать вместо zip архива jar архив.
     * Параметром mf укзывается {@link java.util.jar.Manifest манифест} jar архива, он учитывается только в том случае
     * если jar = true.
     *
     * @param files
     * @param outFileName
     * @param plain
     * @param jar
     * @param mf
     * @throws IOException
     */
    public static void zipFiles(File[] files, String outFileName, boolean plain, boolean jar, Manifest mf) throws IOException {

        byte[] buf = new byte[8 * 1024];

        if (jar) { //todo корректное сохрание имекн файлов в jar архивах в custom JarOutputStream

            JarOutputStream out;
            if (mf != null) {
                out = new JarOutputStream(new FileOutputStream(outFileName), mf);
            } else {
                out = new JarOutputStream(new FileOutputStream(outFileName));
            }
            try {
                for (File f : files) {
                    FileInputStream in = new FileInputStream(f);
                    out.putNextEntry(new JarEntry(plain ? f.getName() : f.toString()));
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                    in.close();
                }
            } finally {
                out.close();
            }

        } else {

            ZipOutputStream out;
            out = new ZipOutputStream(new FileOutputStream(outFileName));
            try {
                for (File f : files) {
                    FileInputStream in = new FileInputStream(f);
                    out.putNextEntry(new ZipEntry(plain ? f.getName() : f.toString()));
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                    in.close();
                }
            } finally {
                out.close();
            }
        }
    }

    public static void jarFile(File file, File outFile) throws IOException {
        jarFiles(new File[]{file}, outFile.toString());
    }

    public static void jarFile(File file, File outFile, boolean plain) throws IOException {
        jarFiles(new File[]{file}, outFile.toString(), plain, null);
    }

    public static void jarFiles(File[] files, String outFileName) throws IOException {
        zipFiles(files, outFileName, false, true, null);
    }

    public static void jarFiles(File[] files, String outFileName, boolean plain, Manifest mf) throws IOException {
        zipFiles(files, outFileName, plain, true, mf);
    }


    /**
     * Распаковывает zip архив заданный параметром zipFile в текущую директорию.
     *
     * @param zipFile
     * @return Возвращает множество ссылок на распакованные из архива файлы.
     * @throws IOException
     */
    public static File[] unzipFile(File zipFile) throws IOException {
        return unzipFile(zipFile, null);
    }

    /**
     * Распаковывает zip архив заданный параметром zipFile в директорию указанную параметром inDir
     *
     * @param zipFile
     * @param inDir
     * @return Возвращает множество ссылок на распакованные из архива файлы.
     * @throws IOException
     */
    public static File[] unzipFile(File zipFile, File inDir) throws IOException {

        Set<File> files = new HashSet<File>();
        ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile));
        java.util.zip.ZipEntry e;
        while ((e = in.getNextEntry()) != null) {
            File fe = (inDir != null) ? new File(inDir, e.getName()) : new File(e.getName());
            if (e.isDirectory()) {
                fe.mkdirs();
            } else {
                files.add(fe);
                if (fe.getParentFile() != null && !fe.getParentFile().exists()) {
                    fe.getParentFile().mkdirs();
                }
                FileOutputStream os = new FileOutputStream(fe);
                try {
                    byte[] buf = new byte[8 * 1024];
                    int r = 0;
                    while ((r = in.read(buf)) != -1) {
                        os.write(buf, 0, r);
                    }
                    os.flush();
                } finally {
                    os.close();
                }
            }
        }

        return files.toArray(new File[files.size()]);
    }

    /**
     * Распаковать Jar, Zip файл
     *
     * @param zipFile файл архива
     * @param inDir   дира куда распаковать
     * @return колецию распакованных файлов без диры
     * @throws IOException ошибка ввода-вывода
     */
    public static File[] unjarFile(File zipFile, File inDir) throws IOException {
        Set<File> files = new HashSet<File>();

        Enumeration entries;
        ZipFile zip;

        zip = new ZipFile(zipFile);
        entries = zip.entries();


        String zipFileName;
        if (zipFile.getName().lastIndexOf(".") != -1) {
            zipFileName = zipFile.getName().substring(0, zipFile.getName().lastIndexOf("."));
        } else {
            zipFileName = zipFile.getName();
        }

        while (entries.hasMoreElements()) {
            java.util.zip.ZipEntry entry = (java.util.zip.ZipEntry) entries.nextElement();
            File fe = (inDir != null) ? new File(inDir + File.separator + zipFileName, entry.getName()) : new File(entry.getName());
            if (entry.isDirectory()) {
                fe.mkdirs();
            } else {
                if (fe.getParentFile() != null && !fe.getParentFile().exists()) {
                    fe.getParentFile().mkdirs();
                }
                files.add(fe);
                IOUtils.copy(zip.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(fe)));
            }
        }

        zip.close();
        return files.toArray(new File[files.size()]);
    }


    /**
     * Обновляем Menifest атрибуты в META-IN
     *
     * @param jarFile JAR файл, файл должен существовать
     * @param mattrs  Map с атрибутами манифеста
     * @throws IOException
     */
    public static void updateJarFileManifest(File jarFile, Map<String, String> mattrs) throws IOException {

        JarFile jf = new JarFile(jarFile);
        File tmp = File.createTempFile(jarFile.getName(), null);

        try {

            Manifest mf = new Manifest();
            Attributes attributes = mf.getMainAttributes();
            Attributes jfattrs = jf.getManifest().getMainAttributes();

            for (final Map.Entry es : jfattrs.entrySet()) {
                attributes.put(es.getKey(), es.getValue());
            }
            for (final Map.Entry<String, String> me : mattrs.entrySet()) {
                attributes.put(new Attributes.Name(me.getKey()), me.getValue());
            }

            JarOutputStream jtmp = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)), mf);
            for (Enumeration entries = jf.entries(); entries.hasMoreElements(); ) {
                JarEntry entry = (JarEntry) entries.nextElement();
                if (!entry.getName().startsWith("META-INF/MANIFEST.MF")) {
                    InputStream entryStream = jf.getInputStream(entry);
                    jtmp.putNextEntry(entry);
                    IOUtils.copy(entryStream, jtmp);
                }
            }

            jtmp.flush();
            jtmp.close();

            try {
                jf.close();
                jarFile.delete();
            } finally {
                FileUtils.moveFile(tmp, jarFile);
            }

        } finally {
            if (tmp.exists()) {
                tmp.delete();
            }
        }
    }


    /**
     * Обновляет содержимое JAR файла
     *
     * @param jarFile      JAR файл, может отсутствовать
     * @param entryPath    Путь до элемента в файле, который нужно обновить или создать
     * @param entryContent Reader содержимое элемента
     * @param meta         Список ключей добавляемых META-INF
     * @throws IOException
     */
    public static void updateJarFile(File jarFile, String entryPath, Reader entryContent) throws IOException {

        if (!jarFile.exists()) {
            Manifest mf = new Manifest();
            Attributes attributes = mf.getMainAttributes();
            attributes.put(new Attributes.Name("Created-By"), "UIS Software (Softmotions Ltd)");
            attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile), mf);
            jos.flush();
            jos.close();
        }
        JarFile jf = new JarFile(jarFile);
        File tmp = File.createTempFile(jarFile.getName(), null);
        try {

            JarOutputStream jtmp = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)));
            JarEntry je = new JarEntry(entryPath);
            je.setTime(System.currentTimeMillis());
            jtmp.putNextEntry(je);
            IOUtils.copy(entryContent, jtmp);

            for (Enumeration entries = jf.entries(); entries.hasMoreElements(); ) {
                JarEntry entry = (JarEntry) entries.nextElement();
                if (!entry.getName().equals(entryPath)) {
                    InputStream entryStream = jf.getInputStream(entry);
                    jtmp.putNextEntry(entry);
                    IOUtils.copy(entryStream, jtmp);
                }
            }

            jtmp.flush();
            jtmp.close();

            try {
                jf.close();
                jarFile.delete();
            } finally {
                FileUtils.moveFile(tmp, jarFile);
            }

        } finally {
            if (tmp.exists()) {
                tmp.delete();
            }
        }
    }
}
