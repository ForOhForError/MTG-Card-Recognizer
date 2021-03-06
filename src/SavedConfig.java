import com.github.sarxos.webcam.Webcam;
import forohfor.scryfall.api.JSONUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Paths;

public class SavedConfig
{
    public static String PATH;
    public static boolean DEBUG;
    public static boolean WRITE_BASICS_TO_SETS = false;

    private static JSONObject CONF_OBJECT;

    @SuppressWarnings("unchecked")
    public static void init()
    {
        generateNewConfig("");
        JSONParser parse = new JSONParser();
        File f = new File("config.json");
        if (f.exists())
        {
            try
            {
                JSONObject root = (JSONObject) parse.parse(new FileReader(f));
                CONF_OBJECT.putAll(root);
                PATH = JSONUtil.getStringData(root, "path");
                DEBUG = JSONUtil.getBoolData(root, "debug");
                WRITE_BASICS_TO_SETS = JSONUtil.getBoolData(root, "write_basics_to_sets");
                WebcamUtils.loadSettings((JSONObject) root.get("webcam_settings"));
            } catch (Exception err)
            {
                err.printStackTrace();
            }
        } else
        {
            JOptionPane.showMessageDialog(null, "Config file not found; doing initial setup.");
            JOptionPane.showMessageDialog(null, "You will be prompted for a directory to save set data to.");
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            chooser.setDialogTitle("Select a directory to save set data to");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                String s = chooser.getSelectedFile().getAbsolutePath();
                if (!s.endsWith(File.separator))
                {
                    s = s + File.separator;
                }
                generateNewConfig(s);
            } else
            {
                System.exit(0);
            }
        }

        if (!DEBUG)
        {
            try
            {
                System.setErr(new PrintStream(new FileOutputStream(new File("errorlog.txt"))));
            } catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }

        writeOut();
    }

    public static RecognitionStrategy getStrat()
    {
        return StrategySelect.getStrat(CONF_OBJECT.get("recognition_strategy").toString());
    }

    public static AreaRecognitionStrategy getAreaStrat()
    {
        return StrategySelect.getAreaStrat(CONF_OBJECT.get("area_recognition_strategy").toString());
    }

    @SuppressWarnings("unchecked")
    public static void setPreferredStrat(RecognitionStrategy strat)
    {
        CONF_OBJECT.put("recognition_strategy", strat.getStratName());
        writeOut();
    }

    @SuppressWarnings("unchecked")
    public static void setPreferredAreaStrat(AreaRecognitionStrategy strat)
    {
        CONF_OBJECT.put("area_recognition_strategy", strat.getStratName());
        writeOut();
    }

    @SuppressWarnings("unchecked")
    private static void generateNewConfig(String path)
    {
        PATH = path;
        DEBUG = false;
        CONF_OBJECT = new JSONObject();
        CONF_OBJECT.put("path", path);
        CONF_OBJECT.put("debug", false);
        CONF_OBJECT.put("write_basics_to_sets", false);
        CONF_OBJECT.put("recognition_strategy", new HashNarrowedRecogStrat().getStratName());
        CONF_OBJECT.put("area_recognition_strategy", new AutoDetectAreaStrat().getStratName());
        JSONObject camconf = new JSONObject();
        camconf.put("cam_name", "");
        camconf.put("cam_resolution_w", -1);
        camconf.put("cam_resolution_h", -1);
        camconf.put("ip_cams", new JSONArray());
        CONF_OBJECT.put("webcam_settings", camconf);
    }

    @SuppressWarnings("unchecked")
    public static void updateWebcamPrefs(Webcam cam, Dimension dim)
    {
        JSONObject camconf = (JSONObject) CONF_OBJECT.get("webcam_settings");
        camconf.put("cam_name", cam.getName());
        camconf.put("cam_resolution_w", dim.width);
        camconf.put("cam_resolution_h", dim.height);
        CONF_OBJECT.put("webcam_settings", camconf);
    }

    @SuppressWarnings("unchecked")
    public static boolean putProperty(String path, Object o)
    {
        String[] ids = path.split("\\.");
        JSONObject obj = CONF_OBJECT;
        for (int i = 0; i < ids.length; i++)
        {
            if (i + 1 == ids.length)
            {
                obj.put(ids[i], o);
                return true;
            }

            if (obj.containsKey(ids[i]))
            {
                obj = (JSONObject) obj.get(ids[i]);
            } else
            {
                JSONObject o2 = new JSONObject();
                obj.put(ids[i], o2);
                obj = o2;
            }
        }
        return false;
    }

    public static Object getProperty(String path, Object defaultValue)
    {
        String[] ids = path.split("\\.");
        JSONObject obj = CONF_OBJECT;
        for (int i = 0; i < ids.length; i++)
        {
            if (obj.containsKey(ids[i]))
            {
                if (i + 1 == ids.length)
                {
                    return obj.get(ids[i]);
                }
                obj = (JSONObject) obj.get(ids[i]);
            } else
            {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static Object getIntProperty(String path, int defaultValue)
    {
        Object result = getProperty(path, null);
        if (result == null)
        {
            return defaultValue;
        } else if (result instanceof Long)
        {
            return ((Long) result).intValue();
        } else if (result instanceof Integer)
        {
            return ((Integer) result).intValue();
        }
        return defaultValue;
    }

    public static void writeOut()
    {
        File f = new File("config.json");
        FileWriter out;
        try
        {
            out = new FileWriter(f);
            out.write(CONF_OBJECT.toJSONString());
            out.flush();
            out.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static String getSubPath(String folderName)
    {
        return Paths.get(PATH, folderName).toString();
    }

    public static String getCustomSetPath(String folderName, String deckName)
    {
        return Paths.get(PATH, folderName, deckName.replace(" ", "_") + "_.yam").toString();
    }

    public static String getSetPath(String setCode)
    {
        // Why add an underscore to the set code? Well, windows disallows
        // certain file names from existing with any extension, including
        // the device name "CON" -- and guess what Conflux's set code is.
        // Thanks, Windows :|
        return Paths.get(PATH, setCode + "_.yam").toString();
    }
}
