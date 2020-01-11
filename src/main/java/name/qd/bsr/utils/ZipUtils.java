package name.qd.bsr.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipUtils {
	private static Logger log = LoggerFactory.getLogger(ZipUtils.class);

	public void zipFolder(String folder) {
		log.info("zipping {}", folder);
		Path path = Paths.get(folder);
		try {
			Path pTarget = Files.createFile(Paths.get(path.getParent().toString(), path.getName(path.getNameCount() - 1) + ".zip"));
			try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(pTarget))) {
				Files.walk(path).filter(p -> !Files.isDirectory(p)).forEach(p -> {
					ZipEntry zipEntry = new ZipEntry(path.relativize(p).toString());
					try {
						zs.putNextEntry(zipEntry);
						Files.copy(p, zs);
						zs.closeEntry();
					} catch (IOException e) {
						log.error("zip {} file failed.", p.toString(), e);
					}
				});
			}
		} catch (IOException e) {
			log.error("create file failed.", e);
		}
		log.info("zip done.");
	}
}
