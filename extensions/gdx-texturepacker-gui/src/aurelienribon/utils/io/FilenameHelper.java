package aurelienribon.utils.io;

import java.io.File;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;

public class FilenameHelper {
	/**
	 * Removes every '"' character before and after the path, if any.
	 */
	public static String trim(String path) {
		while (path.startsWith("\"") && path.endsWith("\""))
			path = path.substring(1, path.length()-1);
		return path;
	}

	/**
	 * Gets the relative path from one file to another.
	 */
	public static String getRelativePath(String targetPath, String basePath) {
		if (basePath == null || basePath.equals("")) return targetPath;
		if (targetPath == null || targetPath.equals("")) return "";

		String pathSeparator = File.separator;

		// Normalize the paths
		String normalizedTargetPath = FilenameUtils.normalizeNoEndSeparator(targetPath);
		String normalizedBasePath = FilenameUtils.normalizeNoEndSeparator(basePath);

		if (basePath.equals(targetPath)) return "";

		// Undo the changes to the separators made by normalization
		if (pathSeparator.equals("/")) {
			normalizedTargetPath = FilenameUtils.separatorsToUnix(normalizedTargetPath);
			normalizedBasePath = FilenameUtils.separatorsToUnix(normalizedBasePath);

		} else if (pathSeparator.equals("\\")) {
			normalizedTargetPath = FilenameUtils.separatorsToWindows(normalizedTargetPath);
			normalizedBasePath = FilenameUtils.separatorsToWindows(normalizedBasePath);

		} else {
			throw new IllegalArgumentException("Unrecognised dir separator '" + pathSeparator + "'");
		}

		String[] base = normalizedBasePath.split(Pattern.quote(pathSeparator));
		String[] target = normalizedTargetPath.split(Pattern.quote(pathSeparator));

		// First get all the common elements. Store them as a string,
		// and also count how many of them there are.
		StringBuilder common = new StringBuilder();

		int commonIndex = 0;
		while (commonIndex < target.length && commonIndex < base.length
			&& target[commonIndex].equals(base[commonIndex])) {
			common.append(target[commonIndex]).append(pathSeparator);
			commonIndex++;
		}

		if (commonIndex == 0) {
			return targetPath;
		}

		// The number of directories we have to backtrack depends on whether the base is a file or a dir
		// For example, the relative path from
		//
		// /foo/bar/baz/gg/ff to /foo/bar/baz
		//
		// ".." if ff is a file
		// "../.." if ff is a directory
		//
		// The following is a heuristic to figure out if the base refers to a file or dir. It's not perfect, because
		// the resource referred to by this path may not actually exist, but it's the best I can do
		boolean baseIsFile = true;

		File baseResource = new File(normalizedBasePath);

		if (baseResource.exists()) {
			baseIsFile = baseResource.isFile();

		} else if (basePath.endsWith(pathSeparator)) {
			baseIsFile = false;
		}

		StringBuilder relative = new StringBuilder();

		if (base.length != commonIndex) {
			int numDirsUp = baseIsFile ? base.length - commonIndex - 1 : base.length - commonIndex;

			for (int i = 0; i < numDirsUp; i++) {
				relative.append("..").append(pathSeparator);
			}
		}
		relative.append(normalizedTargetPath.substring(common.length()));
		return relative.toString();
	}
}
