package com.company;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DupFinder_Main {
    public static double SimilarityValue = 0.7;
    public static int MinCommentSize = 10;

    public static HashSet<String> StopWordsTable;

    public static POSTaggerME tagger;
    public static DictionaryLemmatizer lemmatizer;

    public static ArrayList<JavaDocSegment> DocSegments = new ArrayList<>();

    public static Boolean isUnZip = false;
    public static File file1;

    public static long firstSizeInBytes = -1;
    public static long secondSizeInBytes = -3;

    public static LinkedList<DocCommit> ArrayOfCommits = new LinkedList<>();

    public static DocCommit[] docCommits;

    public static ArrayList<HashSet<JavaDocSegment>> ListOfAllGroups = new ArrayList<>();
    public static ArrayList ResultForLogListOfAllGroups = new ArrayList<>();

    public static HashSet<JavaDocSegment> ALLHashSetListOfListOfAllGroups = new HashSet<>();
    public static HashSet<JavaDocSegment> HashSetListOfListOfAllGroups = new HashSet<>();

    public static ArrayList<ArrayList<ArrayList<DocCommit>>> Result = new ArrayList<>();
    public static ArrayList<DocCommit> TestResult = new ArrayList<>();

    public static void Initialize(String[] args) {

        try {
            tagger = new POSTaggerME(new POSModel(new FileInputStream("en-pos-perceptron.bin")));
            lemmatizer = new DictionaryLemmatizer(new FileInputStream("en-lemmatizer.dict"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CommentsComparison IsNearClones(JavaDocSegment segI, JavaDocSegment segJ) {
        if (segI.Content == segJ.Content)
            return new CommentsComparison(segJ, segJ.NGrams, true);

        float numCommonNgrams = 0;
        if (segI.NGrams.size() < segJ.NGrams.size()) {
            for (String ngram : segI.NGrams) {
                if (IndexMap.get(ngram).contains(segJ))
                    numCommonNgrams++;
            }
        } else {
            for (String ngram : segJ.NGrams) {
                if (IndexMap.get(ngram).contains(segI))
                    numCommonNgrams++;
            }
        }


        if ((numCommonNgrams / segI.NGrams.size()) < SimilarityValue &&
                (numCommonNgrams / segJ.NGrams.size()) < SimilarityValue)
            return new CommentsComparison(segJ, null, false);

        ArrayList<String> archetype = LCS(segI.NGrams, segJ.NGrams);

        if (((float) archetype.size() / segI.NGrams.size() >= SimilarityValue &&
                (float) archetype.size() / segJ.NGrams.size() >= SimilarityValue))
            return new CommentsComparison(segJ, archetype, true);
        else
            return new CommentsComparison(segJ, archetype, false);
    }

    static HashMap<String, HashSet<JavaDocSegment>> IndexMap = null;

    static final Pattern NotDescriptionBody = Pattern.compile("@param|@return|@throws");
    static final Pattern JavaDocPattern = Pattern.compile("(?s)package\\s*(.*?);|(/\\*\\*(?s:(?!\\*/).)*\\*/)(.*?)[;\\{]");
    static final Pattern textWordsPattern = Pattern.compile("(?:^|[(\\[\\s\"/])([A-Za-z]+)(?=$|[\\s)\\],'\"/\\.])", Pattern.MULTILINE);

    public static AbstractMap<JavaDocSegment, HashSet<JavaDocSegment>> MakeDuplicatesTable(ArrayList<JavaDocSegment> DocSegments) {
        long start = System.currentTimeMillis();

        ConcurrentHashMap<JavaDocSegment, HashSet<JavaDocSegment>> DupicatesTable = new ConcurrentHashMap<>();

        IndexMap = BuildSimIndexMap();

        HashMap<JavaDocSegment, HashSet<JavaDocSegment>> CandidateClonesMap = new HashMap<>();
        IndexMap.forEach((k, v) -> {
            v.forEach(segment -> {
                if (CandidateClonesMap.get(segment) == null)
                    CandidateClonesMap.put(segment, new HashSet<>());
                CandidateClonesMap.get(segment).addAll(v);
            });
        });

        DocSegments.forEach(segment -> {
            if (CandidateClonesMap.get(segment) != null) {
                CandidateClonesMap.get(segment).stream().
                        filter(clone -> clone != segment && clone.OrderNumber < segment.OrderNumber).forEach(clone -> {
                    CommentsComparison sim = IsNearClones(segment, clone);
                    if (sim != null && sim.IsNearClones) {
                        if (DupicatesTable.get(segment) == null)
                            DupicatesTable.put(segment, new HashSet<>());
                        DupicatesTable.get(segment).add(clone);
                        if (DupicatesTable.get(clone) == null)
                            DupicatesTable.put(clone, new HashSet<>());
                        DupicatesTable.get(clone).add(segment);
                    }
                });
            }
        });

        long end = System.currentTimeMillis();
        System.out.println("Dups counted in " + (end - start));
        return DupicatesTable;
    }


    public static ArrayList<HashSet<JavaDocSegment>> ExtractDupGroups(AbstractMap<JavaDocSegment, HashSet<JavaDocSegment>> GroupsTable) {
        ArrayList<HashSet<JavaDocSegment>> printedGroups = new ArrayList<>();

        GroupsTable.forEach((k, v) -> {
            v.add(k);
            boolean found = false;
            for (HashSet<JavaDocSegment> group : printedGroups) {
                if (group.equals(v)) {
                    found = true;
                    break;
                }
            }

            if (!found) printedGroups.add(v);

        });
        return printedGroups;
    }

    public static void PrintDupsReport(ArrayList<HashSet<JavaDocSegment>> groups) {
        // Копирую всю группу в новый список (метод глубокого копирования)
        for (HashSet<JavaDocSegment> elements : groups) {
            ListOfAllGroups.add((HashSet<JavaDocSegment>) elements.clone());
        }
        // Это группы
        for (HashSet<JavaDocSegment> group : groups) {
            ArrayList<ArrayList<DocCommit>> ListOfGroup = new ArrayList<>();
            while (group.iterator().hasNext()) {
                Iterator<JavaDocSegment> commitIterator = group.iterator();
                // Это элемент группы
                while (commitIterator.hasNext()) {
                    JavaDocSegment commit = commitIterator.next();
                    List<String> key = Arrays.asList(commit.Signature, commit.Namespace, commit.Location);
                    ArrayList<DocCommit> commitOfGroup = Analyze.dictionary.get(key);
                    if (commitOfGroup != null) {
                        ListOfGroup.add(commitOfGroup);
                    }
                    commitIterator.remove();
                }
                String TempName = null;
                String TempDateTime = null;
                for (ArrayList<DocCommit> commit : ListOfGroup) {
                    if (TempDateTime == null && TempName == null) {
                        TempName = commit.get(0).Name;
                        TempDateTime = commit.get(0).DateTime;
                    } else {
                        if (commit.get(0).Name.equals(TempName) && commit.get(0).DateTime.equals(TempDateTime)) {
                            ListOfGroup.clear();
                            break;
                        }
                    }
                }
                if (ListOfGroup.size() == 1) {
                    if (!Result.contains(ListOfGroup)) {
                        Result.add(ListOfGroup);
                    }
                }
                if (ListOfGroup.size() > 1) {
                    boolean isAdd = false;
                    int simmilarSize = 0;
                    for (ArrayList<DocCommit> commitOfGroup : ListOfGroup) {
                        if (simmilarSize == 0) {
                            simmilarSize = commitOfGroup.size();
                        }
                        if (commitOfGroup.size() != simmilarSize) {
                            Result.add(ListOfGroup);
                            isAdd = true;
                            break;
                        }
                    }
                    boolean isEqual = false;
                    if (!isAdd & simmilarSize > 0) {
                        for (int i = 0; i + 1 < ListOfGroup.size(); i++) {
                            for (int g = 0; g < ListOfGroup.get(i).size(); g++) {
                                isEqual = ListOfGroup.get(i).get(g).DocSegments.get(0).Content.equals(ListOfGroup.get(i + 1).get(g).DocSegments.get(0).Content);
                                if (!isEqual) {
                                    if (!Result.contains(ListOfGroup)) {
                                        Result.add(ListOfGroup);
                                        TestResult.add(ListOfGroup.get(i).get(g));
                                        TestResult.add(ListOfGroup.get(i + 1).get(g));
                                        for (HashSet<JavaDocSegment> HashSetOfListOfAllGroups : ListOfAllGroups) {
                                            if (!HashSetListOfListOfAllGroups.isEmpty()) {
                                                ResultForLogListOfAllGroups.add(HashSetListOfListOfAllGroups);
                                            }
                                            HashSetListOfListOfAllGroups = new HashSet<>();
                                            for (JavaDocSegment ElementOfHashSetOfListOfAllGroups : HashSetOfListOfAllGroups) {
                                                for (ArrayList<DocCommit> ArrayListOfListOfGroup : ListOfGroup) {
                                                    for (DocCommit ArrayListOfArrayListOfListOfGroup : ArrayListOfListOfGroup) {
                                                        if (ElementOfHashSetOfListOfAllGroups.Namespace.equals(ArrayListOfArrayListOfListOfGroup.DocSegments.get(0).Namespace)
                                                                && (ElementOfHashSetOfListOfAllGroups.Signature.equals(ArrayListOfArrayListOfListOfGroup.DocSegments.get(0).Signature)
                                                                && (ElementOfHashSetOfListOfAllGroups.Location.equals(ArrayListOfArrayListOfListOfGroup.DocSegments.get(0).Location)))) {
                                                            if (!ALLHashSetListOfListOfAllGroups.contains(ArrayListOfArrayListOfListOfGroup.DocSegments.get(0))) {
                                                                ALLHashSetListOfListOfAllGroups.add(ArrayListOfArrayListOfListOfGroup.DocSegments.get(0));
                                                                HashSetListOfListOfAllGroups.add(ArrayListOfArrayListOfListOfGroup.DocSegments.get(0));
                                                            }
                                                        } else {
                                                            if (!ALLHashSetListOfListOfAllGroups.contains(ElementOfHashSetOfListOfAllGroups)) {
                                                                ALLHashSetListOfListOfAllGroups.add(ElementOfHashSetOfListOfAllGroups);
                                                                HashSetListOfListOfAllGroups.add(ElementOfHashSetOfListOfAllGroups);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                /*
                int simmilarSize = 0;
                boolean isSizeSimmilar = true;
                for (ArrayList<DocCommit> GroupElement : ListOfGroup) {
                    if (simmilarSize == 0) {
                        simmilarSize = GroupElement.size();
                    }
                    if (GroupElement.size() != simmilarSize)
                    {
                        isSizeSimmilar = false;
                    }
                }
                if (isSizeSimmilar) {
                    int i = 0;
                    while (simmilarSize > i) {
                        HashSet<String> ListOfContent = new HashSet<>();
                        for (ArrayList<DocCommit> GroupElement : ListOfGroup) {
                            ListOfContent.add(GroupElement.get(i).DocSegments.get(0).Content);
                        }
                        if (ListOfContent.size() == 1) {
                            for (ArrayList<DocCommit> GroupElement : ListOfGroup) {
                                GroupElement.remove(i);
                            }
                            simmilarSize--;
                        }
                        else {
                            i++;
                        }
                    }
                }*/
            }
        }

    }

    public static void main(String[] args) {
        System.out.println("Let's the magic begins...");
        try {
            //Инициализируем NLP-штуки для удаления несмысловых слов
            Initialize(args);
            AnalyzeDirectory(args[0]);
            return;

            //long start=System.currentTimeMillis();
            //Парсим исходники в список JavaDocSegment
            //ParseDirectory(args[0]);
            //long end=System.currentTimeMillis();

            //System.out.println("Finished parsing " +(end - start) +" .\nCalculating similarity...");

            //PrintDupsReport распечатывает в PlainDups.txt группы повторов, тебе нужно вместо этого передать это в общий список повторов

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }


    public static ArrayList<String> LCS(final ArrayList<String> s1, final ArrayList<String> s2) {
        int[][] L = new int[s1.size() + 1][s2.size() + 1];

        /* Following steps build L[m+1][n+1] in bottom up fashion. Note
        that L[i][j] contains length of LCS of X[0..i-1] and Y[0..j-1] */
        for (int i = 0; i <= s1.size(); i++) {
            for (int j = 0; j <= s2.size(); j++) {
                if (i == 0 || j == 0)
                    L[i][j] = 0;
                else if (s1.get(i - 1).equals(s2.get(j - 1)))
                    L[i][j] = L[i - 1][j - 1] + 1;
                else
                    L[i][j] = Math.max(L[i - 1][j], L[i][j - 1]);
            }
        }

        // Following code is used to print LCS
        int index = L[s1.size()][s2.size()];

        // Create a character array to store the lcs string
        ArrayList<String> lcs = new ArrayList<>(index + 1);

        // Start from the right-most-bottom-most corner and
        // one by one store characters in lcs[]
        int i = s1.size(), j = s2.size();
        while (i > 0 && j > 0) {
            // If current character in X[] and Y are same, then
            // current character is part of LCS
            if (s1.get(i - 1).equals(s2.get(j - 1))) {
                lcs.add(0, s1.get(i - 1)); // Put current character in result
                i--;
                j--;
                index--;     // reduce values of i, j and index
            }

            // If not same, then find the larger of two and
            // go in the direction of larger value
            else if (L[i - 1][j] > L[i][j - 1])
                i--;
            else
                j--;
        }

        return lcs;
    }


    public static HashMap<String, HashSet<JavaDocSegment>> BuildSimIndexMap() {
        HashMap<String, HashSet<JavaDocSegment>> IndexMap = new HashMap<>();
        DocSegments.forEach(segment -> {
            segment.NGrams.stream().distinct().forEach(NGram -> {
                HashSet<JavaDocSegment> IndexValue = IndexMap.get(NGram);
                if (IndexValue == null)
                    IndexValue = new HashSet<>();

                IndexValue.add(segment);
                IndexMap.put(NGram, IndexValue);
            });
        });
        return IndexMap;
    }


    static ConcurrentHashMap<String, String> ParsedFilesMap = new ConcurrentHashMap<>();


    public static void ParseJavadoc(String block, String range, String signature, String namespace, String path) throws IOException {
        if (block.contains("{@inheritDoc}")) return;
        if (signature.contains("package")) return;

        if (signature != null && block.length() > 0) {
            ArrayList<String> sents = ExtractTextWords(ExtractDescriptionSection(block));
            if (sents.size() >= MinCommentSize) {
                ArrayList<String> POSTags = new ArrayList<>();
                ArrayList<String> TextSentence = RemoveUnnecessaryPOSWords(RemoveStopWords(sents), POSTags);
                sents = LemmatizeWords(TextSentence, POSTags);

                JavaDocSegment segment = new JavaDocSegment(block, sents, range, signature, namespace, path);

                theLock.lock();
                //Присваиваем номера сегментам, чтобы рассматривать только половину матрицы похожести
                segment.OrderNumber = DocSegments.size();

                DocSegments.add(segment);
                theLock.unlock();
            }

        }
    }

    private static String CleanSignature(String fillSig) {
        String strs[] = fillSig.split("\n");
        for (int i = strs.length - 1; i >= 0; i--) {
            if (strs[i].length() > 0) {
                strs[i] = strs[i].replaceAll("(public)|(protected)|(private)|(static)|(final)", "").replaceAll("  ", " ");
                if (strs[i].contains(")")) {
                    int idx = strs[i].indexOf(")") + 1;
                    strs[i] = strs[i].substring(0, idx);
                } else {
                    int idx = strs[i].indexOf("<");
                    if (idx == -1) idx = strs[i].length() - 1;
                    int idx1 = strs[i].lastIndexOf(" ", idx);
                    strs[i] = strs[i].substring(Math.max(0, idx1), idx);
                }
                return strs[i].trim();
            }
        }
        return fillSig;
    }

    private static String ApplyTypeToSignature(String Type, String sig) {
        String res = sig;
        if (sig.contains(" ")) {
            if (sig.contains("(")) {
                int idx = sig.indexOf(" ");
                res = sig.substring(0, idx) + " " + Type + "." + sig.substring(idx + 1, sig.length());
            } else
                res = Type + "." + sig;
        }
        return res;
    }

    private static String GetTypeForIndex(String block, int Offset) {
        int idxClass = block.lastIndexOf("class", Offset);
        int idxEnum = block.lastIndexOf("enum", Offset);
        int idxIface = block.lastIndexOf("interface", Offset);

        int idx = -1;
        if (idxClass > idxEnum && idxClass > idxIface)
            idx = idxClass;
        if (idxClass < idxEnum && idxEnum > idxIface)
            idx = idxEnum;
        if (idxIface > idxEnum && idxClass < idxIface)
            idx = idxIface;

        if (idx == -1) return "";

        int idx1 = block.indexOf("{", idx);
        if (idx1 == -1) idx1 = block.length();
        String Type = CleanSignature(block.substring(idx, idx1));
        if (Type.contains("(")) {
            int fg = 0;
        }
        if (Type.length() > 0)
            Type += ".";
        return Type;
    }

    public static String ExtractDescriptionSection(String block) {
        Matcher matcher = NotDescriptionBody.matcher(block);
        int idx = block.length();
        if (matcher.find()) {
            idx = matcher.start();
        }

        return block.substring(0, idx - 1);
    }


    public static void ParseDirectory(String Path) throws IOException {
        File path = new File(Path);
        if (path.isFile())
            ParseFile(new File(Path));
        else {
            ArrayList<File> files = new ArrayList<>();
            new DirExplorer((level, fpath, file) -> fpath.endsWith(".java"), (level, fpath, file) -> {
                files.add(file);
            }).explore(path);

            files.forEach(file -> {
                try {
                    ParseFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

    }


    public static void ParseFile(File file) throws IOException {
        if (file.getAbsolutePath().contains(".java")) {
            byte[] encodedContent = Files.readAllBytes(file.toPath());
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                String thedigest = new String(md.digest(encodedContent), "UTF-8");
                if (ParsedFilesMap.containsKey(thedigest)) {
                    System.out.println("File " + file.getAbsolutePath() + " is a copy of " + ParsedFilesMap.get(thedigest) + " [SKIPPED]");
                    return;
                }
                ParsedFilesMap.put(thedigest, file.getAbsolutePath());

                String content = new String(encodedContent, StandardCharsets.UTF_8);
                String namespace = "";
                Matcher matcher = JavaDocPattern.matcher(content);
                while (matcher.find()) {
                    if (matcher.group(matcher.groupCount()) == null)
                        namespace = matcher.group(1);
                    else
                        ParseJavadoc(matcher.group(2).intern(), matcher.start() + "-" + matcher.end(), matcher.group(matcher.groupCount()), namespace, file.getAbsolutePath());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static ArrayList<String> ExtractTextWords(String para) {
        Matcher textWordsMatch = textWordsPattern.matcher(para);

        ArrayList<String> TextWords = new ArrayList<>();
        while (textWordsMatch.find()) {
            String tw = textWordsMatch.group(textWordsMatch.groupCount()).trim();
            if (tw.length() > 0)
                TextWords.add(tw.toLowerCase());
        }
        return TextWords;

    }

    public static ArrayList<String> RemoveStopWords(ArrayList<String> words) throws IOException {
        if (StopWordsTable == null) {
            List<String> StopWords = Files.readAllLines(new File("stop_word.txt").toPath(), StandardCharsets.UTF_8);
            StopWordsTable = new HashSet<>(StopWords);
        }

        ArrayList<String> res = new ArrayList<>();
        for (String word : words) {
            if (!StopWordsTable.contains(word))
                res.add(word);
        }

        return res;
    }

    static Lock theLock = new ReentrantReadWriteLock().writeLock();

    public static ArrayList<String> RemoveUnnecessaryPOSWords(ArrayList<String> words, ArrayList<String> POSTags) throws IOException {
        ArrayList<String> res = new ArrayList<>();
        POSTags.clear();
        String[] wordsArr = (String[]) words.toArray(new String[0]);

        theLock.lock();
        String[] posTags = tagger.tag(wordsArr);
        theLock.unlock();

        for (int i = 0; i < posTags.length; i++) {

            if (!posTags[i].equals("CD") && !posTags[i].equals("NNP") && !posTags[i].equals("SYM")) {
                res.add(words.get(i));
                POSTags.add(posTags[i]);
            }
        }
        return res;
    }

    public static ArrayList<String> LemmatizeWords(ArrayList<String> words, ArrayList<String> POSTags) throws IOException {
        String[] lemmas = lemmatizer.lemmatize(words.toArray(new String[0]), POSTags.toArray(new String[0]));
        ArrayList<String> res = new ArrayList<>();
        for (int i = 0; i < lemmas.length; i++) {
            if (!lemmas[i].equals("O"))
                res.add(lemmas[i]);
            else
                res.add(words.get(i));
        }

        return res;
    }

    public static void AnalyzeFile(File file) {
        try {
            String jsonToString = readFileAsString(file.toString());
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new StringReader(jsonToString));
            reader.setLenient(true);
            docCommits = gson.fromJson(reader, DocCommit[].class);
            ArrayOfCommits.add(docCommits[0]);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            file1.delete();
        }
    }

    public static String readFileAsString(String file) throws Exception {
        return new String(Files.readAllBytes(Paths.get(file)));
    }


    public static void AnalyzeDirectory(String Path) {
        File path = new File(Path);
        if (path.isFile())
            AnalyzeFile(new File(Path));
        else {
            File LastfileCommit = new File(Path);
            for (String lastcommitName : LastfileCommit.list()) {
                if (lastcommitName.endsWith(".json")) {
                    file1 = new File(Path + "\\" + lastcommitName);
                    break;
                }
            }
            AnalyzeFile(file1);
            DocSegments = ArrayOfCommits.getLast().DocSegments;
            PrintDupsReport(ExtractDupGroups(MakeDuplicatesTable(DocSegments)));
            /*new DirExplorer((level, fpath, file) -> fpath.endsWith(".zip"), (level, fpath, file) -> files.add(file)).explore(path);

            files.parallelStream().forEachOrdered(file -> {
                try {
                    isUnZip = false;
                    UnZip.UnZip(file);
                    file1 = new File(Path + "\\" + UnZip.UnZip(file));
                    while (!isUnZip) {
                        while (firstSizeInBytes != secondSizeInBytes) {
                            firstSizeInBytes = file1.length();
                            Thread.sleep(50);
                            secondSizeInBytes = file1.length();
                            isUnZip = false;
                        }
                        AnalyzeFile(file1);
                        isUnZip = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                DocSegments = ArrayOfCommits.getLast().DocSegments;
                PrintDupsReport(ExtractDupGroups(MakeDuplicatesTable(DocSegments)));
            });*/
        }
    }
}