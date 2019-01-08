package smp.edgecraft.uhc.core.util;


import java.io.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static smp.edgecraft.uhc.core.util.DNUtils.*;


public class DNData2 implements Cloneable {

    //== CHARACTERS ==//
    /**
     * Split variables by the character (in between var name and expression). Space, tab and dot characters are always used.
     */
    public char bonusSplit = '=';
    /**
     * Whether bonus split should be used when saving.
     */
    public boolean preferBonus = false;
    /**
     * End the level.
     */
    public char lvlEnd = '<';
    /**
     * Start the list.
     */
    public char listBegin = '[';
    /**
     * End the list.
     */
    public char listEnd = ']';
    /**
     * Define macro sequence.
     */
    public String defineMacro = "()";

    //== VARS ==//
    /**
     * Macros that stay in memory.
     */
    public HashMap<String, Function<String, String>> macros = new LinkedHashMap<String, Function<String, String>>() {{
        put("", DNData2::encase);
        put("pi", (input) -> Math.PI + "");
        put("sin", (input) -> ofNum(sin(parseDouble(input))));
        put("cos", (input) -> ofNum(cos(parseDouble(input))));
        put("sqrt", (input) -> ofNum(sqrt(parseDouble(input))));
        put("abs", (input) -> ofNum(abs(parseDouble(input))));
        put("floor", (input) -> ofNum(floor(parseDouble(input))));
        put("ceil", (input) -> ofNum(ceil(parseDouble(input))));
        put("round", (input) -> ofNum(round(parseDouble(input))));
        put("sum", (input) -> {
            String[] args = input.split("\n");
            try {
                double sum = Arrays.stream(args).mapToDouble(Double::parseDouble).sum();
                return ofNum(sum);
            } catch (NumberFormatException e) {
                return DNUtils.join(args, "");
            }
        });
        put("mul", (input) -> {
            String[] args = input.split("\n");
            try {
                double sum = Arrays.stream(args).mapToDouble(Double::parseDouble).reduce((a, b) -> a * b).orElseThrow(NumberFormatException::new);
                return ofNum(sum);
            } catch (NumberFormatException e) {
                return null;
            }
        });
        put("len", (input) -> input.length() + "");
        put("cut", (input) -> {
            String[] args = input.split("\n");
            if (args.length > 2) try {
                int start = Integer.parseInt(args[1]);
                assert start > -1 && start <= args[0].length();
                int end = Integer.parseInt(args[2]);
                assert end >= start;
                return encase(args[0].substring(start, end));
            } catch (IllegalArgumentException e) {
                return encase(args[0]);
            }
            try {
                int length = Integer.parseInt(args[1]);
                assert length > -1;
                return encase(args[0].substring(0, length));
            } catch (IllegalArgumentException e) {
                return encase(args[0]);
            }
        });
        put("from", (input) -> {
            String[] args = input.split("\n");
            try {
                int start = Integer.parseInt(args[1]);
                assert start <= args[0].length();
                return encase(args[0].substring(0, start));
            } catch (IllegalArgumentException e) {
                return encase(args[0]);
            }
        });
        put("index", (input) -> {
            String[] args = input.split("\n");
            return args[0].indexOf(args[1]) + "";
        });
        put("at", (input) -> {
            String[] args = input.split("\n");
            try {
                return encase(args[0].charAt(Integer.parseInt(args[1])) + "");
            } catch (NumberFormatException e) {
                return null;
            }
        });
        put("repeat", (input) -> {
            String[] args = input.split("\n");
            try {
                int amount = Integer.parseInt(args[1]);
                assert amount > -1;
                return encase(repeat(args[0], amount));
            } catch (IllegalArgumentException e) {
                return null;
            }
        });
        put("count", (input) -> {
            String[] args = input.split("\n");
            return count(args[0], args[1]) + "";
        });
        put("concat", (input) -> {
            String[] args = input.split("\n");
            return encase(DNUtils.join(args, ""));
        });
        put("n", (input) -> "\n");
        put("size", (input) -> input.split("\n").length + "");
        put("distinct", (input) -> encase(join(Arrays.stream(input.split("\n")).distinct().collect(Collectors.toList()), "\n")));
        put("filter", (input) -> {
            String[] args = input.split("\n");
            String macro = args[args.length - 1];
            return encase(join(Arrays.stream(args).limit(args.length - 1).filter(e -> {
                String res = evaluate(macro + "(" + encase(e) + ")");
                try {
                    res = (parseDouble(res) != 0) + "";
                } catch (NumberFormatException ignored) {
                }
                return res.equals("true");
            }).collect(Collectors.toList()), "\n"));
        });
        put("map", (input) -> {
            String[] args = input.split("\n");
            String macro = args[args.length - 1];
            return encase(join(Arrays.stream(args).limit(args.length - 1).map(e -> evaluate(macro + "(" + encase(e) + ")")).collect(Collectors.toList()), "\n"));
        });
        put("reduce", (input) -> {
            String[] args = input.split("\n");
            String macro = args[args.length - 1];
            return encase(Arrays.stream(args).limit(args.length - 1).reduce((a, e) -> evaluate(macro + "(" + encase(a + "\n" + e) + ")")).orElse(input));
        });
    }};
    /**
     * Vars that stay in memory.
     */
    public HashMap<String, String> vars = new LinkedHashMap<>();
    /**
     * Backup DNData2 that will be checked through too if value does not exist.
     */
    public DNData2 backup;
    /**
     * File to save to.
     */
    public File file;
    private String currentLevel = "";

    /**
     * Do nothing (use for adding macros and loading later).
     */
    protected DNData2() {
    }

    /**
     * Load DND2 from lines list.
     */
    public DNData2(List<String> lines) {
        loadFrom(lines);
    }

    /**
     * Load DND2 from a file.
     */
    public DNData2(File file) {
        this.file = file;
        try {
            loadFrom(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load DND2 from an input stream.
     */
    public DNData2(InputStream is) {
        loadFrom(is);
    }

    /**
     * Load DND2 from an input stream.
     */
    public void loadFrom(InputStream is) {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
            List<String> lines = new LinkedList<>();
            String line;
            while ((line = r.readLine()) != null) lines.add(line);
            loadFrom(lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load DND2 from lines list.
     */
    public void loadFrom(List<String> lines) {
        lines = lines.stream().map(s -> removeComments(trim(s, ' ', '\t').replace("%nbsp%", " "))).collect(Collectors.toList());
        lines.removeIf(String::isEmpty);
        int i = 1;
        while (i < lines.size()) {
            String line = lines.get(i);
            if (line.contains("@>")) {
                lines.set(i - 1, lines.get(i - 1) + trim(line.substring(line.indexOf("@>") + 2), ' ', '\t'));
                lines.remove(i);
            } else i++;
        }
        run(lines);
    }

    /**
     * Save DND2 execution results into a file.
     */
    public boolean save() {
        return save(false);
    }

    /**
     * Reload DND2 using file's input stream.
     */
    public void reloadFromFile() {
        if (file == null) throw new IllegalArgumentException("Cannot reload fileless DNData");
        try {
            loadFrom(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save DND2 execution results into a file.
     *
     * @param clearPrint All levels are saved as is (with dots, without formatting).
     */
    public boolean save(boolean clearPrint) {
        if (file == null) throw new IllegalArgumentException("Cannot save fileless DNData");
        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try (PrintWriter w = new PrintWriter(file)) {
            if (clearPrint) vars.forEach((k, v) -> {
                boolean c = v.contains("\n");
                if (c) v += "\n" + listEnd;
                w.println(k + (c ? listBegin + "\n" : preferBonus ? bonusSplit : ' ') + v);
            });
            else w.print(toString());
            w.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    protected static String removeComments(String input) {
        int endComment = input.indexOf("//");
        if (endComment >= 0) input = input.substring(0, endComment);
        String[] ss = substringsBetween(input, "/*", "*/");
        if (ss != null) for (String f : ss) input = input.replace("/*" + f + "*/", "");
        return input;
    }

    public void run(List<String> lines) {
        int levelStart = -1;
        String level = "";
        int levels = 0;

        int listStart = -1;
        String list = "";
        boolean inlist = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (inlist) {
                if (charEquals(listEnd, line)) {
                    inlist = false;
                    if (levels > 0) continue;
                    if (list.endsWith(defineMacro)) {
                        String path = prependLevel(list);
                        macros.put(path.substring(0, path.length() - 2), createMacro(path, join(lines.subList(listStart, i), "\n")));
                    } else
                        vars.put(prependLevel(list), join(lines.subList(listStart, i).stream().map(this::evaluate).collect(Collectors.toList()), "\n"));
                }
                continue;
            }
            if (levels > 0) {
                if (charEquals(lvlEnd, line) && --levels == 0) {
                    if (level.endsWith(defineMacro)) {
                        String path = prependLevel(level);
                        macros.put(path.substring(0, path.length() - 2), createMacro(path, lines.subList(levelStart, i)));
                        continue;
                    }
                    String cl = currentLevel;
                    currentLevel = prependLevel(level);
                    run(lines.subList(levelStart, i));
                    currentLevel = cl;
                }
            }
            int sp = minN(line.indexOf(' '), line.indexOf('\t'), line.indexOf(bonusSplit));
            if (sp < 0) {
                if (lastChar(line) == listBegin) {
                    listStart = i + 1;
                    inlist = true;
                    list = line.substring(0, line.length() - 1);
                } else if (!charEquals(lvlEnd, line) && levels++ == 0) {
                    levelStart = i + 1;
                    level = line;
                }
                continue;
            }
            if (levels > 0) continue;
            String varName = line.substring(0, sp);
            String exp = trim(line.substring(sp + 1), ' ', '\t', bonusSplit);
            if (varName.endsWith(defineMacro)) {
                String path = prependLevel(varName);
                macros.put(path.substring(0, path.length() - 2), createMacro(path, exp));
            } else vars.put(prependLevel(varName), evaluate(exp));
        }
    }

    protected String prependLevel(String var) {
        return prependLevel(var, currentLevel);
    }

    protected String prependLevel(String var, String lvl) {
        if (var.charAt(0) == lvlEnd) return var.substring(1);
        return (lvl.isEmpty() ? "" : lvl + ".") + var;
    }

    protected Function<String, String> createMacro(String path, List<String> commands) {
        return (input) -> {
            String cl = currentLevel;
            currentLevel = path;
            run(Collections.singletonList("input \"" + input + "\""));
            run(commands);
            String res = evaluate("return");
            removeLevel(currentLevel);
            currentLevel = cl;
            return encase(res);
        };
    }

    protected static double parseDouble(String x) {
        if (lastChar(x) == '\n') throw new NumberFormatException();
        return Double.parseDouble(x);
    }

    protected Function<String, String> createMacro(String path, String command) {
        return (input) -> {
            String cl = currentLevel;
            currentLevel = path;
            run(Collections.singletonList("input \"" + input + "\""));
            String res = join(Arrays.stream(command.split("\n")).map(this::evaluate).collect(Collectors.toList()), "\n");
            removeLevel(currentLevel);
            currentLevel = cl;
            return encase(res);
        };
    }

    protected static int minN(int... ints) {
        int min = Integer.MAX_VALUE;
        boolean valuesfound = false;
        for (int i : ints) {
            if (i < 0) continue;
            min = min(i, min);
            valuesfound = true;
        }
        if (!valuesfound) return ints[0];
        return min;
    }

    public String evaluate(String input) {
        try {
            List<StackPiece> evaluators = new LinkedList<>();
            StringBuilder word = new StringBuilder();
            String func = "";
            int inparentheses = 0;
            boolean instring = false;
            for (int index = 0, len = input.length(); index < len; index++) {
                char c = input.charAt(index);
                // strings
                if (c == '\"') {
                    if (index == 0 || (!instring || input.charAt(index - 1) != '\\')) {
                        instring = !instring;
                        if (!instring) {
                            word.append(c);
                            continue;
                        }
                    }
                    if (index > 0 && input.charAt(index - 1) == '\\') word.deleteCharAt(word.length() - 1);
                }
                // parentheses
                if (!instring) {
                    if (c == '(' && inparentheses++ == 0) {
                        func = word + "";
                        continue;
                    }
                    if (c == ')' && --inparentheses == 0) {
                        evaluators.add(new GroupStackPiece(func, word.substring(func.length())));
                        word = new StringBuilder();
                        func = "";
                        continue;
                    }
                    // operations
                    if (inparentheses == 0) {
                        OperationStackPiece osp = OperationStackPiece.get(c);
                        if (osp != null) {
                            if (!trim(word + "", ' ', '\t').isEmpty())
                                evaluators.add(new ValueStackPiece(word + ""));
                            else if (evaluators.size() == 0) {
                                if (c == '!') osp = OperationStackPiece.NOT;
                                if (c == '-') osp = OperationStackPiece.UMS;
                            } else if (evaluators.get(evaluators.size() - 1) instanceof OperationStackPiece) {
                                String last = evaluators.get(evaluators.size() - 1) + "";
                                if (c == '=') {
                                    if (last.equals("!"))
                                        evaluators.set(evaluators.size() - 1, OperationStackPiece.NEQ);
                                    if (last.equals("<"))
                                        evaluators.set(evaluators.size() - 1, OperationStackPiece.LEQ);
                                    if (last.equals(">"))
                                        evaluators.set(evaluators.size() - 1, OperationStackPiece.MEQ);
                                }
                                if (c == '>' && last.equals("<"))
                                    evaluators.set(evaluators.size() - 1, OperationStackPiece.NEQ);
                                osp = null;
                                if (c == '!') osp = OperationStackPiece.NOT;
                                if (c == '-') osp = OperationStackPiece.UMS;
                            }
                            if (osp != null) evaluators.add(osp);
                            word = new StringBuilder();
                            continue;
                        }
                    }
                }
                word.append(c);
            }
            if (word.length() > 0) evaluators.add(new ValueStackPiece(word + ""));
            while (evaluators.size() > 1) {
                int importantIndex = 0;
                OperationStackPiece important = null;
                for (int i = 0; i < evaluators.size(); i++) {
                    StackPiece sp = evaluators.get(i);
                    if (sp instanceof OperationStackPiece && (important == null || ((OperationStackPiece) sp).importance > important.importance)) {
                        important = (OperationStackPiece) sp;
                        importantIndex = i;
                    }
                }
                ValueStackPiece a = importantIndex == 0 || important.unary ? null : (ValueStackPiece) evaluators.get(importantIndex - 1);
                ValueStackPiece b = (ValueStackPiece) evaluators.get(importantIndex + 1);
                evaluators.set(importantIndex, new ValueStackPiece(important.evaluatifier.apply(a == null ? null : a.obj, b.obj)));
                evaluators.remove(importantIndex + 1);
                if (importantIndex > 0) evaluators.remove(importantIndex - 1);
            }
            if (((ValueStackPiece) evaluators.get(0)).obj == null) return input;
            return evaluators.get(0) + "";
        } catch (Exception e) {
            return input;
        }
    }

    protected static String ofNum(double d) {
        return d == (long) d ? (long) d + "" : d + "";
    }

    protected static class StackPiece {
    }

    protected class ValueStackPiece extends StackPiece {
        String obj;

        public ValueStackPiece(String obj) {
            this.obj = trim(obj, ' ', '\t');
            if (charEquals('\n', this.obj)) return;
            int piece = -1;
            if (lastChar(this.obj) == listEnd) {
                int pieceStart = this.obj.indexOf(listBegin);
                piece = Integer.parseInt(evaluate(this.obj.substring(pieceStart + 1, this.obj.length() - 1)));
                this.obj = this.obj.substring(0, pieceStart);
            }
            if (this.obj.charAt(0) == '\"' && this.obj.charAt(this.obj.length() - 1) == '\"') {
                this.obj = this.obj.substring(1, this.obj.length() - 1).replace("\\\"", "\"");
                return;
            }
            if (this.obj.contains(" ")) throw new SpaceInLevelHandler();
            try {
                parseDouble(this.obj);
            } catch (NumberFormatException e) {
                String lvl = currentLevel;
                while (lvl.length() > 0 && !vars.containsKey(lvl + "." + this.obj))
                    lvl = lvl.substring(0, max(0, lvl.indexOf('.')));
                String res = vars.get(prependLevel(this.obj, lvl));
                this.obj = piece > -1 ? res.split("\n")[piece] : res;
            }
        }

        public String toString() {
            return obj;
        }
    }

    protected class GroupStackPiece extends ValueStackPiece {
        public GroupStackPiece(String func, String obj) {
            super(getMacro(trim(func, ' ', '\t')).apply(trim(obj, ' ', '\t').isEmpty() ? "" : evaluate(trim(obj, ' ', '\t'))));
        }
    }

    public Function<String, String> getMacro(String path) {
        path = path.replace('.', ' ');
        String lvl = currentLevel;
        while (lvl.length() > 0 && !macros.containsKey(lvl + "." + path))
            lvl = lvl.substring(0, max(0, lvl.indexOf('.')));
        return macros.get(prependLevel(path, lvl));
    }

    protected static class OperationStackPiece extends StackPiece {
        public static final OperationStackPiece ADD = new OperationStackPiece(4, "+", false, (a, b) -> {
            try {
                double start = parseDouble(a);
                double end = parseDouble(b);
                return ofNum(start + end);
            } catch (NumberFormatException e) {
                return encase(a + b);
            }
        });
        public static final OperationStackPiece SUB = new OperationStackPiece(4, "-", false, (a, b) -> {
            try {
                double start = parseDouble(a);
                double end = parseDouble(b);
                return ofNum(start - end);
            } catch (NumberFormatException e) {
                return encase(a.replace(b, ""));
            }
        });
        public static final OperationStackPiece MUL = new OperationStackPiece(5, "*", false, (a, b) -> {
            try {
                double start = parseDouble(a);
                double end = parseDouble(b);
                return ofNum(start * end);
            } catch (NumberFormatException e) {
                int end = Integer.parseInt(b);
                return encase(repeat(a, end));
            }
        });
        public static final OperationStackPiece DIV = new OperationStackPiece(5, "/", false, (a, b) -> {
            try {
                double start = parseDouble(a);
                double end = parseDouble(b);
                return ofNum(start / end);
            } catch (NumberFormatException e) {
                return encase(a.replace(b, "\n"));
            }
        });
        public static final OperationStackPiece MOD = new OperationStackPiece(5, "%", false, (a, b) -> {
            try {
                double start = parseDouble(a);
                double end = parseDouble(b);
                return ofNum(start % end);
            } catch (NumberFormatException e) {
                int pos = a.indexOf(b);
                return encase(pos < 0 ? a : a.substring(0, pos));
            }
        });
        public static final OperationStackPiece LES = new OperationStackPiece(3, "<", false, (a, b) -> {
            try {
                double start = parseDouble(a);
                double end = parseDouble(b);
                return encase(start < end ? "true" : "false");
            } catch (NumberFormatException e) {
                return encase(a.compareTo(b) < 0 ? "true" : "false");
            }
        });
        public static final OperationStackPiece LEQ = new OperationStackPiece(3, "<=", false, (a, b) -> {
            try {
                double start = parseDouble(a);
                double end = parseDouble(b);
                return encase(start <= end ? "true" : "false");
            } catch (NumberFormatException e) {
                return encase(a.compareTo(b) <= 0 ? "true" : "false");
            }
        });
        public static final OperationStackPiece MOR = new OperationStackPiece(3, ">", false, (a, b) -> {
            try {
                double start = parseDouble(a);
                double end = parseDouble(b);
                return encase(start > end ? "true" : "false");
            } catch (NumberFormatException e) {
                return encase(a.compareTo(b) > 0 ? "true" : "false");
            }
        });
        public static final OperationStackPiece MEQ = new OperationStackPiece(3, ">=", false, (a, b) -> {
            try {
                double start = parseDouble(a);
                double end = parseDouble(b);
                return encase(start >= end ? "true" : "false");
            } catch (NumberFormatException e) {
                return encase(a.compareTo(b) >= 0 ? "true" : "false");
            }
        });
        public static final OperationStackPiece EQU = new OperationStackPiece(2, "=", false, (a, b) -> encase(a.equals(b) ? "true" : "false"));
        public static final OperationStackPiece NEQ = new OperationStackPiece(2, "!=", false, (a, b) -> encase(!a.equals(b) ? "true" : "false"));
        public static final OperationStackPiece NOT = new OperationStackPiece(6, "!", true, (a, b) -> {
            try {
                return encase(parseDouble(b) == 0 ? "true" : "false");
            } catch (NumberFormatException e) {
                return encase(!b.equals("true") ? "true" : "false");
            }
        });
        public static final OperationStackPiece UMS = new OperationStackPiece(6, "-", true, (a, b) -> {
            try {
                return ofNum(-parseDouble(b));
            } catch (NumberFormatException e) {
                return encase("-" + b);
            }
        });
        public static final OperationStackPiece AND = new OperationStackPiece(1, "&", false, (a, b) -> {
            try {
                a = (parseDouble(a) != 0) + "";
            } catch (NumberFormatException ignored) {
            }
            try {
                b = (parseDouble(b) != 0) + "";
            } catch (NumberFormatException ignored) {
            }
            return encase(a.equals("true") && b.equals("true") ? "true" : "false");
        });
        public static final OperationStackPiece IOR = new OperationStackPiece(0, "|", false, (a, b) -> {
            try {
                a = (parseDouble(a) != 0) + "";
            } catch (NumberFormatException ignored) {
            }
            try {
                b = (parseDouble(b) != 0) + "";
            } catch (NumberFormatException ignored) {
            }
            return encase(a.equals("true") || b.equals("true") ? "true" : "false");
        });

        public final BiFunction<String, String, String> evaluatifier;
        public String print;
        public int importance;
        public boolean unary;

        OperationStackPiece(int importance, String print, boolean unary, BiFunction<String, String, String> evaluatifier) {
            this.importance = importance;
            this.print = print;
            this.unary = unary;
            this.evaluatifier = evaluatifier;
        }

        public String toString() {
            return print;
        }

        public static OperationStackPiece get(char c) {
            if (c == '+') return ADD;
            if (c == '-') return SUB;
            if (c == '*') return MUL;
            if (c == '/') return DIV;
            if (c == '%') return MOD;
            if (c == '<') return LES;
            if (c == '>') return MOR;
            if (c == '=') return EQU;
            if (c == '!') return NOT;
            if (c == '&') return AND;
            if (c == '|') return IOR;
            return null;
        }
    }

    protected static String encase(String input) {
        return "\"" + input.replace("\"", "\\\"") + "\"";
    }

    protected class SpaceInLevelHandler extends RuntimeException {
    }

    /**
     * Get a string data.
     *
     * @param path Path to search from. Separate levels by dot or space.
     * @return String data or null.
     */
    public String getString(String path) {
        return vars.get(path.replace(' ', '.'));
    }

    /**
     * Check if data by path exists.
     *
     * @param path Path to search from. Separate levels by dot or space.
     * @return True if data is found, false otherwise.
     */
    public boolean exists(String path) {
        return vars.containsKey(path.replace(' ', '.'));
    }

    /**
     * Set data by the path.
     *
     * @param path  Path to search from. Separate levels by dot or space.
     * @param value Value to set. Lists or arrays automatically turn in the format, null removes the data.
     */
    public void set(String path, Object value) {
        set(path, true, value);
    }

    /**
     * Set data by the path.
     *
     * @param path            Path to search from. Separate levels by dot or space.
     * @param rewriteExisting If false, rewrite data only if it is not present. Useful for setting defaults. If false, value of null is ignored.
     * @param value           Value to set. Lists or arrays automatically turn in the format, null removes the data.
     */
    public void set(String path, boolean rewriteExisting, Object value) {
        if (!rewriteExisting && exists(path.replace(' ', '.'))) return;
        if (value instanceof Collection) value = join((Collection<?>) value, "\n");
        else if (value.getClass().isArray()) value = join((Object[]) value, "\n");
        vars.put(path.replace(' ', '.'), value + "");
        save(); // AUTOSAVE
    }

    /**
     * Set multple data pairs by the path.
     *
     * @param path  Path to search from. Separate levels by dot or space.
     * @param pairs Name - Value pairs to set. Lists or arrays automatically turn in the format, null removes the data. The objects must be multiple of 2, where every first of two is name, every second is value.
     */
    public void setUnpresent(String path, Object... pairs) {
        if ((pairs.length & 1) == 1) throw new IllegalArgumentException("Amount of names and values are unequal");
        for (int i = 0; i < pairs.length; i += 2) set(path + " " + pairs[i], false, pairs[i + 1]);
    }

    /**
     * Remove the level and all of its contents. Remove "" clears all lines.
     *
     * @param path Path to search from. Separate levels by dot or space. Unlike getting value, last name is also a level.
     */
    public void removeLevel(String path) {
        vars.keySet().removeIf(key -> key.startsWith(path + "."));
    }

    /**
     * Get all level names in the path.
     *
     * @param path Path to search from. Separate levels by dot or space. Unlike getting value, last name is also a level.
     * @return List of level names found in the path.
     */
    public List<String> getLevels(String path) {
        String finalPath = path.replace(' ', '.');
        return vars.keySet().stream().filter(key -> key.startsWith(finalPath + ".")).map(key -> key.substring(path.length() + 1)).filter(key -> key.contains(".")).map(key -> key.substring(0, key.indexOf('.'))).distinct().collect(Collectors.toList());
    }

    /**
     * Get all values in the path.
     *
     * @param path Path to search from. Separate levels by dot or space. Unlike getting single value, last name is also a level.
     * @return Map of Name, Value containing each data.
     */
    public HashMap<String, String> getValues(String path) {
        String finalPath = path.replace(' ', '.');
        HashMap<String, String> res = new LinkedHashMap<>();
        vars.entrySet().stream().filter(e -> e.getKey().startsWith(finalPath + ".") && !e.getKey().substring(finalPath.length() + 1).contains(".")).distinct().forEach(e -> res.put(e.getKey().substring(finalPath.length() + 1), e.getValue()));
        return res;
    }

    /**
     * Get and parse a string list data.
     *
     * @param path Path to search from. Separate levels by dot or space.
     * @return String list data or empty.
     */
    public List<String> getStringList(String path) {
        String res = getString(path);
        if (res == null || res.isEmpty()) return new ArrayList<>();
        return Arrays.asList(res.split("\n"));
    }

    /**
     * Get and parse a boolean list data.
     *
     * @param path Path to search from. Separate levels by dot or space.
     * @return Boolean list data or empty.
     */
    public List<Boolean> getBooleanList(String path) {
        List<Boolean> list = new LinkedList<>();
        String res = getString(path);
        if (res == null || res.isEmpty()) return new ArrayList<>();
        Arrays.stream(res.split("\n")).map(s -> {
            try {
                return Boolean.parseBoolean(s);
            } catch (NumberFormatException e) {
                return 0;
            }
        }).forEach(s -> list.add((Boolean) s));
        return list;
    }

    /**
     * Get and parse a byte list data.
     *
     * @param path Path to search from. Separate levels by dot or space.
     * @return Byte list data or empty.
     */
    public List<Byte> getByteList(String path) {
        List<Byte> list = new LinkedList<>();
        String res = getString(path);
        if (res == null || res.isEmpty()) return new ArrayList<>();
        Arrays.stream(res.split("\n")).map(s -> {
            try {
                return Byte.parseByte(s);
            } catch (NumberFormatException e) {
                return 0;
            }
        }).forEach(s -> list.add((Byte) s));
        return list;
    }

    /**
     * Get and parse a float list data.
     *
     * @param path Path to search from. Separate levels by dot or space.
     * @return Float list data or empty.
     */
    public List<Float> getFloatList(String path) {
        List<Float> list = new LinkedList<>();
        String res = getString(path);
        if (res == null || res.isEmpty()) return new ArrayList<>();
        Arrays.stream(res.split("\n")).map(s -> {
            try {
                return Float.parseFloat(s);
            } catch (NumberFormatException e) {
                return 0;
            }
        }).forEach(s -> list.add((Float) s));
        return list;
    }

    /**
     * Get and parse a double list data.
     *
     * @param path Path to search from. Separate levels by dot or space.
     * @return Double list data or empty.
     */
    public List<Double> getDoubleList(String path) {
        List<Double> list = new LinkedList<>();
        String res = getString(path);
        if (res == null || res.isEmpty()) return new ArrayList<>();
        Arrays.stream(res.split("\n")).map(s -> {
            try {
                return parseDouble(s);
            } catch (NumberFormatException e) {
                return 0;
            }
        }).forEach(s -> list.add((Double) s));
        return list;
    }

    /**
     * Get and parse a short list data.
     *
     * @param path Path to search from. Separate levels by dot or space.
     * @return Short list data or empty.
     */
    public List<Short> getShortList(String path) {
        List<Short> list = new LinkedList<>();
        String res = getString(path);
        if (res == null || res.isEmpty()) return new ArrayList<>();
        Arrays.stream(res.split("\n")).map(s -> {
            try {
                return Short.parseShort(s);
            } catch (NumberFormatException e) {
                return 0;
            }
        }).forEach(s -> list.add((Short) s));
        return list;
    }

    /**
     * Get and parse a integer list data.
     *
     * @param path Path to search from. Separate levels by dot or space.
     * @return Integer list data or empty.
     */
    public List<Integer> getIntList(String path) {
        List<Integer> list = new LinkedList<>();
        String res = getString(path);
        if (res == null || res.isEmpty()) return new ArrayList<>();
        Arrays.stream(res.split("\n")).map(s -> {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return 0;
            }
        }).forEach(list::add);
        return list;
    }

    /**
     * Get and parse a long list data.
     *
     * @param path Path to search from. Separate levels by dot or space.
     * @return Long list data or empty.
     */
    public List<Long> getLongList(String path) {
        List<Long> list = new LinkedList<>();
        String res = getString(path);
        if (res == null || res.isEmpty()) return new ArrayList<>();
        Arrays.stream(res.split("\n")).map(s -> {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                return 0;
            }
        }).forEach(s -> list.add((Long) s));
        return list;
    }

    /**
     * Get and parse a byte data.
     *
     * @param path Path to search from. Separate levels by dot or space.
     * @return Byte data or null.
     */
    public byte getByte(String path) {
        String result = getString(path);
        try {
            return Byte.parseByte(result);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Get and parse a float data.
     *
     * @param path Path to search from. Separate levels by dot or space.
     * @return Float data or null.
     */
    public float getFloat(String path) {
        String result = getString(path);
        try {
            return Float.parseFloat(result);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Get and parse a double data.
     *
     * @param path Path to search from. Separate levels by dot or space.
     * @return Double data or null.
     */
    public double getDouble(String path) {
        String result = getString(path);
        try {
            return parseDouble(result);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Get and parse a boolean data.
     *
     * @param path Path to search from. Separate levels by dot or space.
     * @return True if data is "true".
     */
    public boolean getBoolean(String path) {
        return Boolean.parseBoolean(getString(path));
    }

    /**
     * Get and parse a short data.
     *
     * @param path Path to search from. Separate levels by dot or space.
     * @return Short data or null.
     */
    public short getShort(String path) {
        String result = getString(path);
        try {
            return Short.parseShort(result);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Get and parse a integer data.
     *
     * @param path Path to search from. Separate levels by dot or space.
     * @return Integer data or null.
     */
    public int getInt(String path) {
        String result = getString(path);
        try {
            return Integer.parseInt(result);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Get and parse a long data.
     *
     * @param path Path to search from. Separate levels by dot or space.
     * @return Long data or null.
     */
    public long getLong(String path) {
        String result = getString(path);
        try {
            return Long.parseLong(result);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Get and parse UUID data.
     *
     * @param path Path to search from. Separate levels by dot or space.
     * @return UUID data parsed from string or null.
     */
    public UUID getUUID(String path) {
        return DNUtils.orElse(getString(path), UUID::fromString);
    }

    /**
     * This will place a backup chain into current one.
     * For example, pushing chain cfg2>backup2 into cfg1>backup1>backup1 will result in cfg1>cfg2>backup2>backup1>backup1.
     *
     * @param backup Chain to push.
     */
    public void pushBackupChain(DNData2 backup) {
        if (backup == null) return;
        DNData2 previous = this.backup;
        this.backup = backup;
        backup.appendBackupChain(previous);
    }

    /**
     * This will place a backup chain after current one.
     * For example, appending chain cfg2>backup2 to cfg1>backup1>backup1 will result in cfg1>backup1>backup1>cfg2>backup2.
     *
     * @param backup Chain to append.
     */
    public void appendBackupChain(DNData2 backup) {
        if (backup == null) return;
        DNData2 child = this;
        while (child.backup != null) child = child.backup;
        child.backup = backup;
    }

    /**
     * This will place a backup dnd into current one with its backup chain being lost.
     * For example, pushing chain cfg2>backup2 into cfg1>backup1>backup1 will result in cfg1>cfg2>backup1>backup1.
     * However, there is no loss if backup chain is already empty - pushing chain cfg2>backup2 into cfg1 will result in cfg1>cfg2>backup2.
     *
     * @param backup DND to push.
     */
    public void lossyPushBackup(DNData2 backup) {
        if (backup == null) return;
        DNData2 previous = this.backup;
        this.backup = backup;
        if (previous != null) backup.backup = previous;
    }

    /**
     * Swap DNData configurations, effecively making all references to other DNData pointing to this config, and vise-versa.
     *
     * @param toSwapWith DNData instance to swap with.
     */
    public void swap(DNData2 toSwapWith) {
        DNData2 clone = toSwapWith.clone();
        toSwapWith.backup = backup;
        toSwapWith.vars = vars;
        toSwapWith.macros = macros;
        toSwapWith.listBegin = listBegin;
        toSwapWith.listEnd = listEnd;
        toSwapWith.defineMacro = defineMacro;
        toSwapWith.lvlEnd = lvlEnd;
        toSwapWith.bonusSplit = bonusSplit;
        toSwapWith.preferBonus = preferBonus;
        toSwapWith.file = file;
        backup = clone.backup;
        vars = clone.vars;
        macros = clone.macros;
        listBegin = clone.listBegin;
        listEnd = clone.listEnd;
        defineMacro = clone.defineMacro;
        lvlEnd = clone.lvlEnd;
        bonusSplit = clone.bonusSplit;
        preferBonus = clone.preferBonus;
        file = clone.file;
    }

    /**
     * Create a binary copy of the object.
     *
     * @return Cloned DNData.
     */
    public DNData2 clone() {
        try {
            return (DNData2) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String toString() {
        return new ToStringsNode("", vars).toString();
    }

    protected class ToStringsNode {
        String level;
        HashMap<String, String> values;

        public ToStringsNode(String level, HashMap<String, String> values) {
            this.level = level;
            this.values = values;
        }

        public String toString() {
            List<String> levels = values.keySet().stream().filter(k -> k.contains(".")).map(k -> k.substring(0, k.indexOf('.'))).distinct().collect(Collectors.toList());
            StringBuilder res = new StringBuilder();
            if (!level.isEmpty()) res.append(level).append('\n');
            values.entrySet().stream().filter(e -> !e.getKey().contains(".")).forEach(e -> res.append(!level.isEmpty() ? "  " : "").append(e.getKey()).append(e.getValue().contains("\n") ? "" : preferBonus ? bonusSplit : ' ').append(tryEncase(e.getValue())).append('\n'));
            levels.forEach(lvl -> {
                HashMap<String, String> nv = new LinkedHashMap<>();
                values.entrySet().stream().filter(e -> e.getKey().startsWith(lvl + ".")).forEach(e -> nv.put(e.getKey().substring(lvl.length() + 1), e.getValue()));
                String newlines = new ToStringsNode(lvl, nv).toString();
                if (!level.isEmpty())
                    Arrays.stream(newlines.split("\n")).forEach(n -> res.append("  ").append(n).append("\n"));
                else res.append(newlines);
            });
            if (!level.isEmpty()) res.append(lvlEnd).append('\n');
            return res + "";
        }

        public String tryEncase(String value) {
            if (value.contains("\n")) {
                StringBuilder sb = new StringBuilder();
                Arrays.stream(value.split("\n")).map(this::tryEncase).forEach(str -> sb.append(!level.isEmpty() ? "    " : "  ").append(str).append('\n'));
                return listBegin + "\n" + sb + (!level.isEmpty() ? "  " : "") + listEnd;
            }
            try {
                parseDouble(value);
                return value;
            } catch (IllegalArgumentException e) {
                return encase(value);
            }
        }
    }


}
