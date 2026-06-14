package com.example.rainsafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends BaseActivity {

    private LinearLayout navHome, navHistory, navSettings, navProfile;
    private ImageView ivHome, ivHistory, ivSettings, ivProfile;
    private TextView tvHome, tvHistory, tvSettings, tvProfile;

    private TextView filterAll, filterLaundry, filterRain, filterSensor;
    private TextView tvStatsLaundryCount, tvStatsTotalDuration, tvStatsRainCount;
    private TextView tvHistoryEmpty;
    private RecyclerView rvHistory;
    private HistoryAdapter adapter;

    private DatabaseHelper dbHelper;
    private String currentFilter = "all";
    private boolean showAllToday = false;
    private android.widget.Button btnMoreToday;

    // All logs fetched from DB
    private List<Map<String, String>> allLogs = new ArrayList<>();

    // Connection status
    private View ivStatusDot;
    private TextView tvStatusText;
    private ValueEventListener connectivityListener;
    private static final String DB_URL = "https://rainsafe-777f2-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            View bottomNav = findViewById(R.id.bottomNavContainer);
            if (bottomNav != null) {
                int bottomInset = systemBars.bottom;
                bottomNav.setPadding(bottomNav.getPaddingLeft(), bottomNav.getPaddingTop(), bottomNav.getPaddingRight(), bottomInset);
                android.view.ViewGroup.LayoutParams params = bottomNav.getLayoutParams();
                int baseHeight = (int) android.util.TypedValue.applyDimension(
                    android.util.TypedValue.COMPLEX_UNIT_DIP, 75, v.getResources().getDisplayMetrics());
                params.height = baseHeight + bottomInset;
                bottomNav.setLayoutParams(params);
            }
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        // Navigation Views
        navHome     = findViewById(R.id.navHome);
        navHistory  = findViewById(R.id.navHistory);
        navSettings = findViewById(R.id.navSettings);
        navProfile  = findViewById(R.id.navProfile);

        ivHome     = findViewById(R.id.ivHome);
        ivHistory  = findViewById(R.id.ivHistory);
        ivSettings = findViewById(R.id.ivSettings);
        ivProfile  = findViewById(R.id.ivProfile);

        tvHome     = findViewById(R.id.tvHome);
        tvHistory  = findViewById(R.id.tvHistory);
        tvSettings = findViewById(R.id.tvSettings);
        tvProfile  = findViewById(R.id.tvProfile);

        // Filter Tabs
        filterAll     = findViewById(R.id.filterAll);
        filterLaundry = findViewById(R.id.filterLaundry);
        filterRain    = findViewById(R.id.filterRain);
        filterSensor  = findViewById(R.id.filterSensor);

        // Statistics TextViews
        tvStatsLaundryCount  = findViewById(R.id.tvStatsLaundryCount);
        tvStatsTotalDuration = findViewById(R.id.tvStatsTotalDuration);
        tvStatsRainCount     = findViewById(R.id.tvStatsRainCount);

        // History RecyclerView
        tvHistoryEmpty = findViewById(R.id.tvHistoryEmpty);
        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(new ArrayList<>());
        rvHistory.setAdapter(adapter);
        btnMoreToday = findViewById(R.id.btnMoreToday);
        btnMoreToday.setOnClickListener(v -> {
            showAllToday = true;
            refreshList();
            btnMoreToday.setVisibility(View.GONE);
        });

        // Menu Button
        findViewById(R.id.btnMenu).setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE",       getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
        });

        findViewById(R.id.btnNotifications).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        // Refresh button - reload data + reconnect Firebase
        findViewById(R.id.btnRefresh).setOnClickListener(v -> {
            loadData();
            FirebaseDatabase.getInstance(DB_URL).goOnline();
            Toast.makeText(this, "Data diperbarui", Toast.LENGTH_SHORT).show();
        });

        // Connection status
        ivStatusDot = findViewById(R.id.ivStatusDot);
        tvStatusText = findViewById(R.id.tvStatusText);
        startListeningConnection();

        // Set History as Active (Position 1)
        updateNavUI(1);

        // Navigation Listeners
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE",       getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        navHistory.setOnClickListener(v -> updateNavUI(1));

        navSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE",       getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE",       getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        // Filter Click Listeners
        View.OnClickListener filterClick = v -> {
            resetFilterUI(filterAll, filterLaundry, filterRain, filterSensor);
            v.setBackgroundResource(R.drawable.button_background);
            ((TextView) v).setTextColor(ContextCompat.getColor(this, R.color.white));
            ((TextView) v).setTypeface(null, android.graphics.Typeface.BOLD);

            int id = v.getId();
            if      (id == R.id.filterAll)     currentFilter = "all";
            else if (id == R.id.filterLaundry) currentFilter = "laundry";
            else if (id == R.id.filterRain)    currentFilter = "rain";
            else if (id == R.id.filterSensor)  currentFilter = "sensor";

            refreshList();
        };

        filterAll.setOnClickListener(filterClick);
        filterLaundry.setOnClickListener(filterClick);
        filterRain.setOnClickListener(filterClick);
        filterSensor.setOnClickListener(filterClick);

        // Load real data from DB
        loadData();
    }

    // ─── DATA LOADING ────────────────────────────────────────────────────────

    /**
     * Fetch all logs once, then compute weekly stats and build the list.
     */
    private void loadData() {
        allLogs = dbHelper.getAllLogs();
        updateWeeklyStats();
        refreshList();
    }

    /**
     * Compute and display the three weekly stat cards from real log data.
     */
    private void updateWeeklyStats() {
        int laundryCount = 0;
        int rainCount    = 0;
        long totalDurationMinutes = 0;

        String lastOutTime = null;

        // allLogs is ordered newest first. To calculate duration reliably, it might be easier
        // to iterate oldest to newest.
        for (int i = allLogs.size() - 1; i >= 0; i--) {
            Map<String, String> log = allLogs.get(i);
            String icon = log.get("icon");
            String timeStr = log.get("time"); // format "HH:mm"
            
            if ("out".equals(icon)) {
                lastOutTime = timeStr;
            } else if ("in".equals(icon)) {
                laundryCount++;
                if (lastOutTime != null && timeStr != null) {
                    try {
                        String[] outParts = lastOutTime.split(":");
                        String[] inParts = timeStr.split(":");
                        int outH = Integer.parseInt(outParts[0]);
                        int outM = Integer.parseInt(outParts[1]);
                        int inH = Integer.parseInt(inParts[0]);
                        int inM = Integer.parseInt(inParts[1]);
                        
                        int diffM = (inH * 60 + inM) - (outH * 60 + outM);
                        if (diffM < 0) diffM += 24 * 60; // assumed crossed midnight
                        totalDurationMinutes += diffM;
                        lastOutTime = null;
                    } catch (Exception e) {
                        // ignore parse errors
                    }
                }
            } else if ("rain".equals(icon)) {
                rainCount++;
            }
        }

        String durationStr;
        if (totalDurationMinutes < 60) {
            durationStr = totalDurationMinutes + " mnt";
        } else {
            long h = totalDurationMinutes / 60;
            long m = totalDurationMinutes % 60;
            durationStr = h + "j " + m + "m";
        }

        tvStatsLaundryCount.setText(laundryCount + "x");
        tvStatsTotalDuration.setText(durationStr);
        tvStatsRainCount.setText(rainCount + "x");
    }

    /**
     * Filter logs, group them under date headers, then push to the adapter.
     *
     * Grouping strategy: logs are stored newest-first (ORDER BY id DESC).
     * We use index buckets:
     *   0            → HARI INI   (most recent up to index 4)
     *   5–9          → KEMARIN
     *   10+          → 2 HARI LALU
     *
     * (Adjust bucket sizes to taste.)
     */
    private void refreshList() {
        // 1. Filter
        List<Map<String, String>> filtered = new ArrayList<>();
        for (Map<String, String> log : allLogs) {
            if (matchesFilter(log)) filtered.add(log);
        }

        // 2. Build display list with section headers
        List<Object> displayList = new ArrayList<>();

        // Buckets
        List<Map<String, String>> today     = new ArrayList<>();
        List<Map<String, String>> yesterday = new ArrayList<>();
        List<Map<String, String>> older     = new ArrayList<>();

        int todayLimit = showAllToday ? filtered.size() : 3;
        for (int i = 0; i < filtered.size(); i++) {
            if (i < todayLimit)       today.add(filtered.get(i));
            else if (i < todayLimit + 5) yesterday.add(filtered.get(i));
            else                         older.add(filtered.get(i));
        }

        if (!today.isEmpty()) {
            displayList.add("HARI INI");
            displayList.addAll(today);
        }
        // Show 'Lihat Selengkapnya' button when the filtered list contains more than the default shown today items
        if (!showAllToday && filtered.size() > 3 && btnMoreToday != null) {
            btnMoreToday.setVisibility(View.VISIBLE);
        } else if (btnMoreToday != null) {
            btnMoreToday.setVisibility(View.GONE);
        }
        if (!yesterday.isEmpty()) {
            displayList.add("KEMARIN");
            displayList.addAll(yesterday);
        }
        if (!older.isEmpty()) {
            displayList.add("2 HARI LALU");
            displayList.addAll(older);
        }

        // 3. Show/hide empty state
        if (displayList.isEmpty()) {
            tvHistoryEmpty.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
        } else {
            tvHistoryEmpty.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
        }

        adapter.setData(displayList);
    }

    private boolean matchesFilter(Map<String, String> log) {
        String icon = log.get("icon");
        if (icon == null) icon = "";
        switch (currentFilter) {
            case "laundry": return "in".equals(icon) || "out".equals(icon);
            case "rain":    return "rain".equals(icon);
            case "sensor":  return "sensor".equals(icon);
            default:        return true; // "all"
        }
    }

    // ─── UI HELPERS ──────────────────────────────────────────────────────────

    private void resetFilterUI(TextView... filters) {
        for (TextView tv : filters) {
            tv.setBackgroundResource(R.drawable.button_white_bg);
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
            tv.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    private void updateNavUI(int position) {
        int grey = ContextCompat.getColor(this, R.color.text_grey);
        int blue = ContextCompat.getColor(this, R.color.button_blue);

        ivHome.setColorFilter(grey);
        ivHistory.setColorFilter(grey);
        ivSettings.setColorFilter(grey);
        ivProfile.setColorFilter(grey);

        tvHome.setTextColor(grey);
        tvHistory.setTextColor(grey);
        tvSettings.setTextColor(grey);
        tvProfile.setTextColor(grey);

        tvHome.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvHistory.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvSettings.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvProfile.setTypeface(null, android.graphics.Typeface.NORMAL);

        switch (position) {
            case 0:
                ivHome.setColorFilter(blue);
                tvHome.setTextColor(blue);
                tvHome.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case 1:
                ivHistory.setColorFilter(blue);
                tvHistory.setTextColor(blue);
                tvHistory.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case 2:
                ivSettings.setColorFilter(blue);
                tvSettings.setTextColor(blue);
                tvSettings.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case 3:
                ivProfile.setColorFilter(blue);
                tvProfile.setTextColor(blue);
                tvProfile.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
        }
    }

    // ─── CONNECTION STATUS ────────────────────────────────────────────────────

    private void startListeningConnection() {
        connectivityListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                runOnUiThread(() -> {
                    if (ivStatusDot != null) {
                        ivStatusDot.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(
                                connected ? android.graphics.Color.parseColor("#4CAF50")
                                          : android.graphics.Color.parseColor("#F44336")
                            )
                        );
                    }
                    if (tvStatusText != null) {
                        tvStatusText.setText(connected
                            ? getString(R.string.dash_status_connected)
                            : getString(R.string.dash_status_disconnected));
                        tvStatusText.setTextColor(connected
                            ? ContextCompat.getColor(HistoryActivity.this, R.color.button_blue)
                            : android.graphics.Color.parseColor("#F44336"));
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        };
        FirebaseDatabase.getInstance(DB_URL)
            .getReference(".info/connected").addValueEventListener(connectivityListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectivityListener != null) {
            FirebaseDatabase.getInstance(DB_URL)
                .getReference(".info/connected").removeEventListener(connectivityListener);
        }
    }
}