import java.io.*;
import java.nio.file.*;
import java.util.regex.*;
import java.util.*;

public class FileOrganizer {

    public static void main(String[] args) {
        // Spécifiez ici le chemin du dossier à parcourir
        String sourceFolder = "./com"; // Exemple : dossier contenant les fichiers .java

        try {
            compileJavaFiles(sourceFolder);
        } catch (Exception e) {
            System.err.println("Une erreur s'est produite : " + e.getMessage());
        }
    }

    public static void deleteFiles(String extension,String folderPath) throws IOException {
        File folder = new File(folderPath);
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Le chemin spécifié n'est pas un dossier : " + folderPath);
        }
        File[] files = folder.listFiles();
        for(File file:files) {
            if(file.isDirectory()) {
                deleteFiles(extension,folderPath+"/"+file.getName());
            }
            if(file.getName().endsWith(extension)) {
                file.delete();
            }
        }

    }

    public static void organizeJavaFiles(String folderPath) throws IOException {
        File folder = new File(folderPath);

        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Le chemin spécifié n'est pas un dossier : " + folderPath);
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".java")); // Filtrer les fichiers .java
        if (files == null || files.length == 0) {
            System.out.println("Aucun fichier .java trouvé dans le dossier.");
            return;
        }

        for (File file : files) {
            String packageName = getPackageName(file);
            if (packageName != null) {
                moveFileToPackageFolder(file, folderPath, packageName);
            } else {
                System.out.println("Aucun package trouvé pour le fichier : " + file.getName());
            }
        }
    }

    private static String getPackageName(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();
            if (firstLine != null) {
                // Vérifier si la première ligne correspond au format du package
                Pattern packagePattern = Pattern.compile("^\s*package\s+([a-zA-Z0-9_.]+)\s*;");
                Matcher matcher = packagePattern.matcher(firstLine);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier : " + file.getName());
        }
        return null;
    }

    private static void moveFileToPackageFolder(File file, String baseFolder, String packageName) {
        // Convertir le nom du package en chemin (par exemple, "ast.node" devient "ast/node")
        String packagePath = "src/"+packageName.replace('.', File.separatorChar);
        File targetFolder = new File(baseFolder, packagePath);

        // Créer les dossiers nécessaires
        if (!targetFolder.exists() && !targetFolder.mkdirs()) {
            System.err.println("Impossible de créer les dossiers pour le package : " + packageName);
            return;
        }

        // Déplacer le fichier dans le dossier cible
        File targetFile = new File(targetFolder, file.getName());
        try {
            Files.move(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Fichier déplacé : " + file.getName() + " -> " + targetFile.getPath());
        } catch (IOException e) {
            System.err.println("Erreur lors du déplacement du fichier : " + file.getName());
        }
    }

    public static void compileJavaFiles(String folderPath) throws IOException, InterruptedException {
        File folder = new File(folderPath);

        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Le chemin spécifié n'est pas un dossier : " + folderPath);
        }

        // Trouver tous les fichiers .java dans le dossier source et ses sous-dossiers
        List<String> javaFiles = new ArrayList<>();
        Files.walk(Paths.get(folderPath))
            .filter(path -> path.toString().endsWith(".java"))
            .forEach(path -> javaFiles.add(path.toString()));

        if (javaFiles.isEmpty()) {
            System.out.println("Aucun fichier .java trouvé pour compilation.");
            return;
        }

        // Dossier de sortie pour les fichiers .class
        String outputDir = "./bin";
        File binDir = new File(outputDir);
        if (!binDir.exists()) {
            binDir.mkdirs();
        }

        // Construire la commande javac pour compiler tous les fichiers à la fois
        List<String> command = new ArrayList<>();
        command.add("javac");
        command.add("-d");
        command.add(outputDir);
        command.addAll(javaFiles);

        // Exécuter le compilateur
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();

        // Lire les sorties et erreurs
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
            }
            while ((line = outputReader.readLine()) != null) {
                System.out.println(line);
            }
        }

        // Attendre la fin de la compilation
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            System.out.println("Compilation réussie pour tous les fichiers.");
        } else {
            System.err.println("Erreur lors de la compilation.");
        }
    }

}
