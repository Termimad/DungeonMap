package externius.rdmg.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import externius.rdmg.R;
import externius.rdmg.helpers.Export;
import externius.rdmg.models.RoomDescription;
import externius.rdmg.models.TrapDescription;
import externius.rdmg.views.DungeonMapView;

public class DungeonActivity extends AppCompatActivity {
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static boolean saved = false;
    private static final String SMALL = "Small";
    private static final String MEDIUM = "Medium";
    private static final String LARGE = "Large";
    private static int dungeonDifficulty;
    private static int partyLevel;
    private static int partySize;
    private static int dungeonSize;
    private static int roomDensity;
    private static int roomSize;
    private static int traps;
    private static boolean hasCorridor;
    private static boolean hasDeadEnds;
    private static String monsterType;
    private static DungeonMapView dungeonView;
    private static String jsonMonster;
    private static String jsonTreasure;
    private static double treasureValue;
    private static int itemsRarity;
    private static WeakReference<DungeonActivity> activity;
    private static CreateDungeon createDungeonTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dungeon);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RelativeLayout layout = findViewById(R.id.dungeon_layout);
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.generating_screen);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(relativeParams);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        layout.addView(imageView);
        createDungeonTask = new CreateDungeon(this);
        createDungeonTask.execute();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (createDungeonTask != null) {
            createDungeonTask.cancel(true);
        }
        this.finish();
    }

    static class CreateDungeon extends AsyncTask<Void, Void, Object> {

        CreateDungeon(DungeonActivity activity) {
            DungeonActivity.activity = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            Bundle extras = activity.get().getIntent().getExtras(); // get extra parameters
            setParameters(extras); //set parameters from parent activity
        }

        @Override
        protected Object doInBackground(Void... args) {
            jsonMonster = readJSON(R.raw.monsters);
            jsonTreasure = readJSON(R.raw.treasures);
            return getDungeonView();
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result != null) {
                RelativeLayout layout = activity.get().findViewById(R.id.dungeon_layout);
                layout.removeAllViews();
                layout.addView((View) result);
                addButton(layout);
                addDescription(layout, dungeonView.getRoomDescription(), dungeonView.getTrapDescription());
                layout.setBackgroundColor(Color.WHITE);
            }
        }

        private static String readJSON(int id) {
            if (activity.get() == null) {
                return null;
            }
            String result;
            try (InputStream is = activity.get().getResources().openRawResource(id)) {
                Scanner scanner = new Scanner(is);
                StringBuilder sb = new StringBuilder();
                while (scanner.hasNextLine()) {
                    sb.append(scanner.nextLine());
                }
                result = sb.toString();
            } catch (Exception ex) {
                Log.e("Exception", "Read JSON file failed: " + ex.toString());
                return null;
            }
            return result;
        }

        private static Boolean setBooleans(String input) {
            return input != null && !input.isEmpty() && Objects.equals(input.toUpperCase(), "YES");
        }

        private static int setDungeonDifficulty(String dd) {
            if (dd != null && !dd.isEmpty()) {
                switch (dd) {
                    case "Easy":
                        return 0;
                    case MEDIUM:
                        return 1;
                    case "Hard":
                        return 2;
                    case "Deadly":
                        return 3;
                    default:
                        break;
                }
            }
            return 0;
        }

        private static int setDungeonSize(String ds) {
            if (ds != null && !ds.isEmpty()) {
                switch (ds) {
                    case SMALL:
                        return 15;
                    case MEDIUM:
                        return 20;
                    case LARGE:
                        return 25;
                    default:
                        break;
                }
            }
            return 15;
        }

        private static int setRoomDensity(String rd) {
            if (rd != null && !rd.isEmpty()) {
                switch (rd) {
                    case "Low":
                        return 20;
                    case MEDIUM:
                        return 30;
                    case "High":
                        return 40;
                    default:
                        break;
                }
            }
            return 20;
        }

        private static int setRoomSize(String rs) {
            if (rs != null && !rs.isEmpty()) {
                switch (rs) {
                    case SMALL:
                        return 20;
                    case MEDIUM:
                        return 25;
                    case LARGE:
                        return 30;
                    default:
                        break;
                }
            }
            return 20;
        }

        private static int setTraps(String tr) {
            if (tr != null && !tr.isEmpty()) {
                switch (tr) {
                    case "None":
                        return 0;
                    case "Few":
                        return 15;
                    case "More":
                        return 30;
                    default:
                        break;
                }
            }
            return 0;
        }

        private static double setTreasureValue(String tV) {
            if (tV != null && !tV.isEmpty()) {
                switch (tV) {
                    case "Low":
                        return 0.5;
                    case "Standard":
                        return 1;
                    case "High":
                        return 1.5;
                    default:
                        break;
                }
            }
            return 0;
        }

        private static int setItemsRarity(String iR) {
            if (iR != null && !iR.isEmpty()) {
                switch (iR) {
                    case "Common":
                        return 0;
                    case "Uncommon":
                        return 1;
                    case "Rare":
                        return 2;
                    case "Very Rare":
                        return 3;
                    case "Legendary":
                        return 4;
                    default:
                        break;
                }
            }
            return 0;
        }

        private static void setParameters(Bundle extras) {
            String mt = extras.getString("MONSTER_TYPE");
            dungeonDifficulty = setDungeonDifficulty(extras.getString("DUNGEON_DIFFICULTY"));
            dungeonSize = setDungeonSize(extras.getString("DUNGEON_SIZE"));
            roomDensity = setRoomDensity(extras.getString("ROOM_DENSITY"));
            roomSize = setRoomSize(extras.getString("ROOM_SIZE"));
            traps = setTraps(extras.getString("TRAPS"));
            hasDeadEnds = setBooleans(extras.getString("DEAD_ENDS"));
            hasCorridor = setBooleans(extras.getString("CORRIDORS"));
            partySize = Integer.parseInt(extras.getString("PARTY_SIZE"));
            partyLevel = Integer.parseInt(extras.getString("PARTY_LEVEL"));
            treasureValue = setTreasureValue(extras.getString("TREASURE_VALUE"));
            itemsRarity = setItemsRarity(extras.getString("ITEMS_RARITY"));
            if (mt != null && !mt.isEmpty()) {
                monsterType = mt.toLowerCase();
            }
            saved = false;
        }

    }

    private static void generateDungeon(RelativeLayout layout) {
        if (activity.get().findViewById(R.id.dungeonMap_view) != null) {
            layout.removeAllViews();
            saved = false;
        }
        layout.addView(getDungeonView());
        addButton(layout);
        addDescription(layout, dungeonView.getRoomDescription(), dungeonView.getTrapDescription());
    }

    private static void addButton(RelativeLayout layout) {
        Button button = new Button(activity.get()); //create generate button
        button.setText(R.string.generate_button_text);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.BELOW, R.id.dungeonMap_view);
        button.setLayoutParams(relativeParams);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RelativeLayout layout = activity.get().findViewById(R.id.dungeon_layout);
                generateDungeon(layout);
            }
        });
        button.setId(R.id.dungeon_activity_generate_button);
        buttonStyle(button);
        layout.addView(button);
        button = new Button(activity.get()); // create export button
        button.setText(R.string.export_button_text);
        button.setId(R.id.dungeon_activity_export_button);
        relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.BELOW, R.id.dungeon_activity_generate_button);
        button.setLayoutParams(relativeParams);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkPermission();
            }
        });
        buttonStyle(button);
        layout.addView(button);
    }

    @SuppressWarnings("deprecation")
    private static void buttonStyle(Button button) {
        button.setTextColor(Color.WHITE);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        if (Build.VERSION.SDK_INT < 23) {
            drawable.setColor(activity.get().getResources().getColor(R.color.primaryAccent));
            drawable.setStroke(4, activity.get().getResources().getColor(R.color.primaryDivider));
        } else {
            drawable.setColor(activity.get().getResources().getColor(R.color.primaryAccent, null));
            drawable.setStroke(4, activity.get().getResources().getColor(R.color.primaryDivider, null));
        }
        button.setBackgroundDrawable(drawable);
    }


    private static void checkPermission() {
        if (ContextCompat.checkSelfPermission(activity.get(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity.get(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(activity.get(), "To save the dungeon, the app needs write permission", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(activity.get(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            export();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            export();
        } else {
            Toast.makeText(DungeonActivity.this, "Write files permission is denied", Toast.LENGTH_LONG).show();
        }
    }

    private static void export() {
        if (isExternalStorageWritableAndHasSpace()) {
            Bitmap dungeonBitmap = getBitmapFromView(activity.get().findViewById(R.id.dungeonMap_view));
            File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/DungeonMaps/");
            String html = Export.generateHTML(dungeonBitmap, dungeonView.getRoomDescription(), dungeonView.getTrapDescription());
            if (!directory.exists()) {
                if (directory.mkdirs()) {
                    createFile(directory, getFilename(directory), html);
                }
            } else {
                createFile(directory, getFilename(directory), html);
            }
        }
    }

    private static String getFilename(File directory) {
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".html");
            }
        });
        if (files.length > 0) {
            return "dungeon" + files.length + ".html";
        } else {
            return "dungeon.html";
        }
    }

    private static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    private static boolean isExternalStorageWritableAndHasSpace() {
        String state = Environment.getExternalStorageState();
        long space = Environment.getExternalStorageDirectory().getFreeSpace() / 1024 / 1024;
        return Environment.MEDIA_MOUNTED.equals(state) && space > 1;
    }

    private static void createFile(File directory, String filename, String html) {
        File file = new File(directory, filename);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(html.getBytes());
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        if (saved) {
            Toast.makeText(activity.get(), "You are already saved this dungeon", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity.get(), filename + " saved at: " + directory.getPath(), Toast.LENGTH_LONG).show();
            saved = true;
        }
    }


    private static DungeonMapView getDungeonView() {
        if (activity.get() == null) {
            return null;
        }
        Display display = activity.get().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int area;
        if (size.x > size.y) {
            area = size.y;
        } else {
            area = size.x;
        }
        dungeonView = new DungeonMapView(activity.get());
        dungeonView.setDungeonHeight(area);
        dungeonView.setDungeonWidth(area);
        dungeonView.setDungeonSize(dungeonSize);
        dungeonView.setHasCorridor(hasCorridor);
        dungeonView.setJsonMonster(jsonMonster);
        dungeonView.setJsonTreasure(jsonTreasure);
        dungeonView.setRoomDensity(roomDensity);
        dungeonView.setRoomSizePercent(roomSize);
        dungeonView.setTrapPercent(traps);
        dungeonView.setPartyLevel(partyLevel);
        dungeonView.setItemsRarity(itemsRarity);
        dungeonView.setTreasureValue(treasureValue);
        dungeonView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        dungeonView.setDungeonDifficulty(dungeonDifficulty);
        dungeonView.setPartySize(partySize);
        dungeonView.setMonsterType(monsterType);
        dungeonView.setHasDeadEnds(hasDeadEnds);
        dungeonView.setId(R.id.dungeonMap_view);
        dungeonView.GenerateDungeon();
        return dungeonView;
    }


    private static void addDescription(RelativeLayout layout, List<RoomDescription> roomDescription, List<TrapDescription> trapDescription) {
        List<TextView> rooms = new ArrayList<>();
        for (RoomDescription room : roomDescription) { // generate rooms TextViews
            TextView roomName = new TextView(activity.get());
            TextView monster = new TextView(activity.get());
            TextView treasure = new TextView(activity.get());
            TextView doors = new TextView(activity.get());
            roomName.setText(room.getName());
            roomName.setId(View.generateViewId());
            monster.setText(room.getMonster());
            monster.setId(View.generateViewId());
            treasure.setText(room.getTreasure());
            treasure.setId(View.generateViewId());
            doors.setText(room.getDoors());
            doors.setId(View.generateViewId());
            setTextStyle(roomName);
            setTextStyle(monster);
            setTextStyle(treasure);
            setTextStyle(doors);
            rooms.add(roomName);
            rooms.add(monster);
            rooms.add(treasure);
            rooms.add(doors);
        }
        addViewToLayout(layout, rooms.get(0), null, true, activity.get().findViewById(R.id.dungeon_activity_export_button));
        addViewToLayout(layout, rooms.get(1), activity.get().findViewById(R.id.dungeon_activity_export_button), false, rooms.get(0));
        addViewToLayout(layout, rooms.get(2), rooms.get(1), false, rooms.get(0));
        addViewToLayout(layout, rooms.get(3), rooms.get(2), false, rooms.get(0));
        for (int i = 4; i < rooms.size(); i += 4) { // 4 because the first 4 manually added
            addViewToLayout(layout, rooms.get(i), rooms.get(i - 3), true, rooms.get(i - 1));
            addViewToLayout(layout, rooms.get(i + 1), rooms.get(i - 1), false, rooms.get(i));
            addViewToLayout(layout, rooms.get(i + 2), rooms.get(i + 1), false, rooms.get(i));
            addViewToLayout(layout, rooms.get(i + 3), rooms.get(i + 2), false, rooms.get(i));
        }
        if (!trapDescription.isEmpty()) {
            List<TextView> trapsD = new ArrayList<>();
            for (TrapDescription trap : trapDescription) { // generate traps TextViews
                TextView trapName = new TextView(activity.get());
                TextView trapDes = new TextView(activity.get());
                trapName.setText(trap.getName());
                trapName.setId(View.generateViewId());
                trapDes.setText(trap.getDescription());
                trapDes.setId(View.generateViewId());
                setTextStyle(trapName);
                setTextStyle(trapDes);
                trapsD.add(trapName);
                trapsD.add(trapDes);

            }
            addViewToLayout(layout, trapsD.get(0), null, true, rooms.get(rooms.size() - 1));
            addViewToLayout(layout, trapsD.get(1), rooms.get(rooms.size() - 1), false, trapsD.get(0));
            for (int i = 2; i < trapsD.size(); i += 2) { // 2 because the first 2 manually added
                addViewToLayout(layout, trapsD.get(i), null, true, trapsD.get(i - 1));
                addViewToLayout(layout, trapsD.get(i + 1), trapsD.get(i - 1), false, trapsD.get(i));
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static void setTextStyle(TextView textView) {
        if (Build.VERSION.SDK_INT < 23) {
            textView.setTextColor(activity.get().getResources().getColor(R.color.primaryText));
        } else {
            textView.setTextColor(activity.get().getResources().getColor(R.color.primaryText, null));
        }
    }

    private static void addViewToLayout(RelativeLayout relativeLayout, View view, View parent, boolean root, View end) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (root) {
            params.addRule(RelativeLayout.BELOW, end.getId());
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
        } else {
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            params.addRule(RelativeLayout.END_OF, end.getId());
            params.addRule(RelativeLayout.BELOW, parent.getId());
        }
        relativeLayout.addView(view, params);
    }

}
