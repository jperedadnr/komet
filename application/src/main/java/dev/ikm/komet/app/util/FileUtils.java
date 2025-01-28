package dev.ikm.komet.app.util;

import com.gluonhq.attach.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtils {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);
    private final File assetsFolder;

    public FileUtils() {
        assetsFolder = new File(StorageService.create()
                .flatMap(StorageService::getPrivateStorage)
                .orElseThrow(() -> new RuntimeException("Error accessing Private Storage folder")), "Solor");

        if (!assetsFolder.exists()) {
            if (!assetsFolder.mkdir()) {
                LOG.error("Error creating assets folder {}", assetsFolder.getAbsolutePath());
            }
        }
    }

    public void copyFileFromAssets(String fileName, boolean unzip) {
        if (!getFileFromAssets(fileName).exists()) {
            FutureTask<Boolean> futureTask = new FutureTask<>(new CopyFile(fileName)) {
                @Override
                protected void done() {
                    try {
                        LOG.info("Copying file {} finished, with result: {}", fileName, get());
                    } catch (InterruptedException | ExecutionException e) {
                        LOG.error("Error running task", e);
                    }
                }
            };
            try (ExecutorService exec = Executors.newFixedThreadPool(1)) {
                Future<?> submit = exec.submit(futureTask);
                try {
                    submit.get();
                } catch (InterruptedException | ExecutionException e) {
                    LOG.error("Error while waiting for thread completion", e);
                }
                if (unzip) {
                    try {
                        unzipFile(getFileFromAssets(fileName).toPath());
                    } catch (IOException e) {
                        LOG.error("Error unzipping file {}", fileName, e);
                    }
                }
            }
        } else {
            LOG.info("file {} already exists", fileName);
        }
    }

    public boolean checkFileInResources(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        String pathIni = filePath;
        if (!filePath.startsWith("/")) {
            pathIni = "/" + pathIni;
        }
        return (FileUtils.class.getResource(pathIni) != null);
    }

    public File getFileFromAssets(String filePath) {
        return new File(assetsFolder, filePath.replaceAll("/", "_"));
    }

    private static void unzipFile(Path sourceZip) throws IOException {
        Objects.requireNonNull(sourceZip);
        if (!Files.exists(sourceZip)) {
            throw new IOException("Error: " + sourceZip + " does not exist");
        }
        Path targetDir = sourceZip.getParent();
        Objects.requireNonNull(targetDir);
        if (Files.isRegularFile(targetDir)) {
            throw new IOException("Error: " + targetDir + " is not a directory");
        }
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        try (ZipFile zip = new ZipFile(sourceZip.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File f = new File(targetDir.resolve(Path.of(entry.getName())).toString());
                if (entry.isDirectory()) {
                    if (!f.isDirectory() && !f.mkdirs()) {
                        throw new IOException("Error: failed to create directory " + f);
                    }
                } else {
                    File parent = f.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Error: failed to create directory " + parent);
                    }
                    try (InputStream in = zip.getInputStream(entry)) {
                        Files.copy(in, f.toPath());
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("Error unzipping from " + sourceZip + " into " + targetDir + ": " + e.getMessage() + "\n"
                    + Arrays.toString(e.getSuppressed()));
        }
    }

    private class CopyFile implements Callable<Boolean> {

        private final String filePath;

        public CopyFile(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public Boolean call() throws Exception {
            if (filePath == null || filePath.isEmpty()) {
                LOG.warn("Error, file path was null or empty");
                return Boolean.FALSE;
            }

            File file = getFileFromAssets(filePath);
            if (!file.exists()) {
                LOG.info("Copying file: {}, from resources to {}", filePath, file.getAbsolutePath());
                String pathIni = filePath;
                if (!filePath.startsWith("/")) {
                    pathIni = "/" + pathIni;
                }
                if (!copyFile(pathIni, file.getAbsolutePath())) {
                    LOG.warn("Error, copying file {} to {} failed", pathIni, file.getAbsolutePath());
                    return Boolean.FALSE;
                }
            }
            return Boolean.TRUE;
        }

        private boolean copyFile(String pathIni, String pathEnd) {
            try (InputStream myInput = FileUtils.class.getResourceAsStream(pathIni)) {
                if (myInput == null) {
                    LOG.warn("Error copying file, input was null");
                    return false;
                }
                try (OutputStream myOutput = new FileOutputStream(pathEnd)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = myInput.read(buffer)) > 0) {
                        myOutput.write(buffer, 0, length);
                    }
                    myOutput.flush();
                    return true;
                } catch (IOException ex) {
                    LOG.warn("Error copying file", ex);
                }
            } catch (IOException ex) {
                LOG.warn("Error copying file", ex);
            }
            LOG.warn("Error copying file, something was wrong");
            return false;
        }
    }
}
