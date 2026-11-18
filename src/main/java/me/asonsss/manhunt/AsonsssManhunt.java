package me.asonsss.manhunt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.StructureType;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Полный исходный код плагина: Asonsss_manhunt (Версия 1.5.0)
 *
 * ======================== 1. ФАЙЛ src/main/resources/plugin.yml ========================
 * name: Asonsss_manhunt
 * version: '1.5.0'
 * main: me.asonsss.manhunt.AsonsssManhunt
 * api-version: '1.21'
 * description: Manhunt plugin v1.5.0 with /sp restart, 200-block ocean seed filter, 10s death replay, /stop & /resume, and cross-dimension locatorBar fix
 * commands:
 *   manhunt:
 *     description: Основная команда управления Manhunt
 *     aliases: [speedrun]
 *     usage: /manhunt <game|speedruner|hunter|difficulty|info> ...
 *   sp:
 *     description: Команды спидранера для настройки сложности, перезапуска мира и выбора места старта
 *     usage: /sp <difficulty|pickplace|restart> ...
 *   ready:
 *     description: Подтвердить готовность к началу игры
 *     usage: /ready
 *   unready:
 *     description: Отменить готовность к началу игры
 *     usage: /unready
 *   fortrest:
 *     description: Найти ближайшую крепость Незера (только для спидранера на сложности Easy)
 *     usage: /fortrest
 *   pause:
 *     description: Поставить игру на паузу (до 180 секунд)
 *     aliases: [p]
 *     usage: /pause <секунды>
 *   compass:
 *     description: Получить компас охотника во время игры
 *     aliases: [c]
 *     usage: /compass
 *   stop:
 *     description: Надолго остановить игру Manhunt с сохранением состояния
 *     usage: /stop
 *   resume:
 *     description: Возобновить остановленную игру Manhunt
 *     usage: /resume
 *
 * ======================== 2. ФАЙЛ pom.xml (для сборки через Maven) ========================
 * <project xmlns="http://maven.apache.org/POM/4.0.0"
 *          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
 *     <modelVersion>4.0.0</modelVersion>
 *     <groupId>me.asonsss</groupId>
 *     <artifactId>Asonsss_manhunt</artifactId>
 *     <version>1.5.0</version>
 *     <packaging>jar</packaging>
 *     <properties>
 *         <maven.compiler.source>21</maven.compiler.source>
 *         <maven.compiler.target>21</maven.compiler.target>
 *         <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
 *     </properties>
 *     <repositories>
 *         <repository>
 *             <id>spigot-repo</id>
 *             <url>https://hub.spigotmc.org/nexus/content/repositories/snapshots/</url>
 *         </repository>
 *     </repositories>
 *     <dependencies>
 *         <dependency>
 *             <groupId>org.spigotmc</groupId>
 *             <artifactId>spigot-api</artifactId>
 *             <version>1.21.1-R0.1-SNAPSHOT</version>
 *             <scope>provided</scope>
 *         </dependency>
 *     </dependencies>
 * </project>
 * ==========================================================================================
 */
public class AsonsssManhunt extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    public enum GameState {
        IDLE,
        PREPARE,
        DIFFICULTY_SELECTED,
        PICKING_PLACE,
        WAITING_READY,
        COUNTDOWN,
        IN_GAME,
        REPLAYING_DEATH,
        ENDED
    }

    public enum DifficultyMode {
        EASY("EASY", ChatColor.GREEN, 30.0, 10000.0, 1000.0),
        MEDIUM("MEDIUM", ChatColor.YELLOW, 30.0, 9000.0, 500.0),
        HARD("HARD", ChatColor.RED, 20.0, 8000.0, 500.0),
        IMPOSSIBLE("IMPOSSIBLE", ChatColor.DARK_PURPLE, 20.0, 30000000.0, 500.0);

        private final String displayName;
        private final ChatColor color;
        private final double speedrunnerMaxHealth;
        private final double gameBorderSize;
        private final double pickPlaceBorderSize;

        DifficultyMode(String displayName, ChatColor color, double speedrunnerMaxHealth, double gameBorderSize, double pickPlaceBorderSize) {
            this.displayName = displayName;
            this.color = color;
            this.speedrunnerMaxHealth = speedrunnerMaxHealth;
            this.gameBorderSize = gameBorderSize;
            this.pickPlaceBorderSize = pickPlaceBorderSize;
        }

        public String getDisplayName() {
            return displayName;
        }

        public ChatColor getColor() {
            return color;
        }

        public String getColoredDisplayName() {
            return color + displayName;
        }

        public double getSpeedrunnerMaxHealth() {
            return speedrunnerMaxHealth;
        }

        public double getGameBorderSize() {
            return gameBorderSize;
        }

        public double getPickPlaceBorderSize() {
            return pickPlaceBorderSize;
        }

        public static DifficultyMode fromString(String input) {
            if (input == null) return null;
            switch (input.toLowerCase(Locale.ROOT)) {
                case "easy":
                    return EASY;
                case "medium":
                    return MEDIUM;
                case "hard":
                    return HARD;
                case "impossible":
                    return IMPOSSIBLE;
                default:
                    return null;
            }
        }
    }

    /**
     * Неизменяемая координата блока для отслеживания порталов и изменений блоков в повторе.
     */
    private static final class BlockPos {
        private final String worldName;
        private final int x;
        private final int y;
        private final int z;

        public BlockPos(String worldName, int x, int y, int z) {
            this.worldName = worldName;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public static BlockPos of(Block block) {
            return new BlockPos(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        }

        public Block toBlock() {
            World w = Bukkit.getWorld(worldName);
            return w != null ? w.getBlockAt(x, y, z) : null;
        }

        public String serialize() {
            return worldName + ":" + x + ":" + y + ":" + z;
        }

        public static BlockPos deserialize(String s) {
            if (s == null) return null;
            String[] parts = s.split(":");
            if (parts.length != 4) return null;
            try {
                return new BlockPos(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BlockPos)) return false;
            BlockPos that = (BlockPos) o;
            return x == that.x && y == that.y && z == that.z && Objects.equals(worldName, that.worldName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(worldName, x, y, z);
        }
    }

    // =========================================================================
    // СТРУКТУРЫ ДАННЫХ ДЛЯ 10-СЕКУНДНОГО ПОВТОРА СМЕРТИ (DEATH REPLAY)
    // =========================================================================

    private static final class PlayerSnapshot {
        final UUID uuid;
        final String name;
        final String displayName;
        final Location location;
        final boolean sneaking;
        final boolean swungHand;
        final boolean hurt;
        final ItemStack helmet;
        final ItemStack chestplate;
        final ItemStack leggings;
        final ItemStack boots;
        final ItemStack mainHand;
        final ItemStack offHand;

        PlayerSnapshot(Player p, boolean swungHand, boolean hurt) {
            this.uuid = p.getUniqueId();
            this.name = p.getName();
            this.displayName = p.getDisplayName();
            this.location = p.getLocation().clone();
            this.sneaking = p.isSneaking();
            this.swungHand = swungHand;
            this.hurt = hurt;
            EntityEquipment eq = p.getEquipment();
            this.helmet = eq != null && eq.getHelmet() != null ? eq.getHelmet().clone() : null;
            this.chestplate = eq != null && eq.getChestplate() != null ? eq.getChestplate().clone() : null;
            this.leggings = eq != null && eq.getLeggings() != null ? eq.getLeggings().clone() : null;
            this.boots = eq != null && eq.getBoots() != null ? eq.getBoots().clone() : null;
            this.mainHand = eq != null && eq.getItemInMainHand() != null ? eq.getItemInMainHand().clone() : null;
            this.offHand = eq != null && eq.getItemInOffHand() != null ? eq.getItemInOffHand().clone() : null;
        }
    }

    private static final class EntitySnapshot {
        final UUID uuid;
        final EntityType type;
        final Location location;
        final ItemStack droppedItem;
        final ItemStack helmet;
        final ItemStack chestplate;
        final ItemStack leggings;
        final ItemStack boots;
        final ItemStack mainHand;

        EntitySnapshot(Entity entity) {
            this.uuid = entity.getUniqueId();
            this.type = entity.getType();
            this.location = entity.getLocation().clone();
            if (entity instanceof Item) {
                this.droppedItem = ((Item) entity).getItemStack().clone();
            } else {
                this.droppedItem = null;
            }
            if (entity instanceof LivingEntity) {
                EntityEquipment eq = ((LivingEntity) entity).getEquipment();
                this.helmet = eq != null && eq.getHelmet() != null ? eq.getHelmet().clone() : null;
                this.chestplate = eq != null && eq.getChestplate() != null ? eq.getChestplate().clone() : null;
                this.leggings = eq != null && eq.getLeggings() != null ? eq.getLeggings().clone() : null;
                this.boots = eq != null && eq.getBoots() != null ? eq.getBoots().clone() : null;
                this.mainHand = eq != null && eq.getItemInMainHand() != null ? eq.getItemInMainHand().clone() : null;
            } else {
                this.helmet = null;
                this.chestplate = null;
                this.leggings = null;
                this.boots = null;
                this.mainHand = null;
            }
        }
    }

    private static final class BlockChangeRecord {
        final BlockPos pos;
        final BlockData beforeData;
        final BlockData afterData;

        BlockChangeRecord(BlockPos pos, BlockData beforeData, BlockData afterData) {
            this.pos = pos;
            this.beforeData = beforeData;
            this.afterData = afterData;
        }
    }

    private static final class ReplayFrame {
        final List<PlayerSnapshot> players = new ArrayList<>();
        final List<EntitySnapshot> entities = new ArrayList<>();
        final List<BlockChangeRecord> blockChanges = new ArrayList<>();
    }

    private static final String SOUND_ELECTRIC_PIANO = "block.note_block.pling";
    private static final String SOUND_LEVELUP = "entity.player.levelup";
    private static final String SOUND_WITHER_SPAWN = "entity.wither.spawn";
    private static final String SOUND_CHALLENGE_COMPLETE = "ui.toast.challenge_complete";
    private static final String SOUND_PLAYER_HURT = "entity.player.hurt";

    // Высота нот (pitch) электропианино для обратного отсчёта:
    private static final float PITCH_C_OCTAVE_1 = 0.707107f; // 15-5 сек: C (первая гамма)
    private static final float PITCH_E_OCTAVE_1 = 0.890899f; // 4 сек: E (первая гамма)
    private static final float PITCH_G_OCTAVE_2 = 1.059463f; // 3 сек: G (вторая гамма)
    private static final float PITCH_C_OCTAVE_2 = 1.414214f; // 2 сек: C (вторая гамма)
    private static final float PITCH_E_OCTAVE_2 = 1.781797f; // 1 сек: E (вторая гамма)

    private static final String TEAM_SPEEDRUNNER = "mh_speedrunner";
    private static final String TEAM_HUNTERS = "mh_hunters";
    private static final String OBJECTIVE_SIDEBAR = "mh_sidebar";

    private static final double PRE_GAME_BORDER_SIZE = 30.0;
    private static final double DEFAULT_MINECRAFT_BORDER = 30000000.0; // 30 миллионов блоков
    private static final long IMPOSSIBLE_TIME_LIMIT_SECONDS = 4L * 60L * 60L; // 4 часа = 14400 секунд
    private static final int MAX_PAUSE_SECONDS = 180;
    private static final long PAUSE_COOLDOWN_MILLIS = 10L * 60L * 1000L; // 10 минут
    private static final long COMBAT_PAUSE_LOCK_MILLIS = 60L * 1000L; // 1 минута после удара
    private static final int REPLAY_BUFFER_TICKS = 200; // 10 секунд = 200 тиков
    private static final double REPLAY_CAPTURE_RADIUS = 32.0;
    private static final int OCEAN_CHECK_RADIUS = 200; // Проверка отсутствия океана в радиусе 200 блоков от (0, 0)

    // Координаты коробки ожидания 20x20x20 из барьеров на высоте 250 вокруг (0, 0)
    private static final int BOX_MIN_X = -10;
    private static final int BOX_MAX_X = 9;
    private static final int BOX_MIN_Y = 250;
    private static final int BOX_MAX_Y = 269;
    private static final int BOX_MIN_Z = -10;
    private static final int BOX_MAX_Z = 9;

    // Резервный пул проверенных сидов без океана в центре
    private static final long[] FALLBACK_LAND_SEEDS = {
            10837491238471L, -491827364519283L, 847261593847162L, 293847561029384L,
            -918273645019283L, 564738291019283L, 738291048576123L, -192837465564738L
    };

    private GameState gameState = GameState.IDLE;
    private DifficultyMode selectedDifficulty = null;

    private UUID speedrunnerUuid = null;
    private final Set<UUID> hunterUuids = new HashSet<>();
    private final Set<UUID> readyPlayers = new HashSet<>();

    private final Map<World.Environment, Location> lastRunnerLocations = new EnumMap<>(World.Environment.class);
    private final Set<PotionEffectType> trackedRunnerEffects = new HashSet<>();

    // Отслеживание порталов спидранера (Верхний мир и первоначальный портал в Аду)
    private final Set<BlockPos> runnerOverworldPortalBlocks = new HashSet<>();
    private final Set<BlockPos> runnerInitialNetherPortalBlocks = new HashSet<>();
    private Location runnerInitialNetherPortalCenter = null;
    private long lastWrongPortalWarningMillis = 0L;
    private long lastBorderWarningMillis = 0L;

    private World activeOverworld = null;
    private Location chosenStartLocation = null;
    private boolean waitingBoxBuilt = false;
    private final Random random = new Random();

    private BukkitTask countdownTask = null;
    private BukkitTask gameLoopTask = null;
    private BukkitTask scoreboardAndTimerTask = null;
    private BukkitTask replayRecordingTask = null;
    private BukkitTask replayPlaybackTask = null;

    // Кольцевой буфер для записи последних 10 секунд (200 тиков) перед смертью спидранера
    private final LinkedList<ReplayFrame> replayFrames = new LinkedList<>();
    private final List<BlockChangeRecord> pendingBlockChangesThisTick = new ArrayList<>();
    private final Set<UUID> swungHandThisTick = new HashSet<>();
    private final Set<UUID> hurtThisTick = new HashSet<>();
    private final Map<UUID, ArmorStand> replayPlayerClones = new HashMap<>();
    private final Map<UUID, Entity> replayEntityClones = new HashMap<>();
    private final Map<UUID, ItemStack> cachedPlayerSkulls = new HashMap<>();

    // Таймер игры / обратного отсчёта IMPOSSIBLE (в секундах)
    private long elapsedGameSeconds = 0L;
    private boolean scoreboardActive = false;

    // Переменные системы паузы (/pause, /p), длительной остановки (/stop, /resume) и боевого таймера
    private boolean prePauseActive = false;
    private boolean gamePaused = false;
    private boolean longTermStopped = false;
    private long pauseCooldownEndMillis = 0L;
    private long combatCooldownEndMillis = 0L;
    private BukkitTask pauseTask = null;
    private BossBar pauseBossBar = null;
    private BossBar stopBossBar = null;
    private final Map<UUID, Collection<PotionEffect>> savedEffectsDuringPause = new HashMap<>();
    private final Map<UUID, Location> frozenLocationsDuringStop = new HashMap<>();

    private NamespacedKey compassKey;
    private File savedStateFile;

    @Override
    public void onEnable() {
        this.compassKey = new NamespacedKey(this, "hunter_compass");
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        this.savedStateFile = new File(getDataFolder(), "saved_state.yml");

        Bukkit.getPluginManager().registerEvents(this, this);

        registerCommand("manhunt");
        registerCommand("sp");
        registerCommand("ready");
        registerCommand("unready");
        registerCommand("fortrest");
        registerCommand("pause");
        registerCommand("compass");
        registerCommand("stop");
        registerCommand("resume");

        setupScoreboardTeams();
        startCompassAndEffectTask();
        startScoreboardAndTimerTask();
        startReplayRecordingTask();

        loadSavedGameStateIfPresent();

        getLogger().info("Asonsss_manhunt v1.5.0 успешно запущен!");
    }

    @Override
    public void onDisable() {
        if (longTermStopped && gameState == GameState.IN_GAME) {
            saveGameStateToDisk();
        } else {
            cancelCountdown();
            stopPauseImmediately(false);
            cleanupReplayEntities();
            removeWaitingBox();
            clearSidebarScoreboard();
            resetAllPlayerStates();
        }
        if (gameLoopTask != null) {
            gameLoopTask.cancel();
            gameLoopTask = null;
        }
        if (scoreboardAndTimerTask != null) {
            scoreboardAndTimerTask.cancel();
            scoreboardAndTimerTask = null;
        }
        if (replayRecordingTask != null) {
            replayRecordingTask.cancel();
            replayRecordingTask = null;
        }
        if (replayPlaybackTask != null) {
            replayPlaybackTask.cancel();
            replayPlaybackTask = null;
        }
    }

    private void registerCommand(String name) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }
    }

    private void setupScoreboardTeams() {
        try {
            Scoreboard mainBoard = Bukkit.getScoreboardManager() != null
                    ? Bukkit.getScoreboardManager().getMainScoreboard()
                    : null;
            if (mainBoard == null) return;

            Team srTeam = mainBoard.getTeam(TEAM_SPEEDRUNNER);
            if (srTeam == null) {
                srTeam = mainBoard.registerNewTeam(TEAM_SPEEDRUNNER);
            }
            srTeam.setColor(ChatColor.RED);
            srTeam.setPrefix(ChatColor.RED.toString());

            Team hunterTeam = mainBoard.getTeam(TEAM_HUNTERS);
            if (hunterTeam == null) {
                hunterTeam = mainBoard.registerNewTeam(TEAM_HUNTERS);
            }
            hunterTeam.setColor(ChatColor.BLUE);
            hunterTeam.setPrefix(ChatColor.BLUE.toString());
        } catch (Throwable t) {
            getLogger().warning("Не удалось инициализировать команды Scoreboard: " + t.getMessage());
        }
    }

    // =========================================================================
    // ФИЛЬТРАЦИЯ СИДОВ (ОТСУТСТВИЕ ОКЕАНА В РАДИУСЕ 200 БЛОКОВ ОТ 0:0) И /sp restart
    // =========================================================================

    /**
     * Проверяет, нет ли биомов океана в радиусе 200 блоков от (0, 0).
     * Вызов через рефлексию безопасен для 1.21.3+ / 26.3, где Biome стал интерфейсом.
     */
    private boolean isWorldOceanFreeInRadius200(World world) {
        if (world == null) return false;
        int step = 25;
        int rSq = OCEAN_CHECK_RADIUS * OCEAN_CHECK_RADIUS;
        try {
            Method getBiomeMethod = world.getClass().getMethod("getBiome", int.class, int.class, int.class);
            for (int x = -OCEAN_CHECK_RADIUS; x <= OCEAN_CHECK_RADIUS; x += step) {
                for (int z = -OCEAN_CHECK_RADIUS; z <= OCEAN_CHECK_RADIUS; z += step) {
                    if (x * x + z * z > rSq) continue;
                    Object biomeObj = getBiomeMethod.invoke(world, x, 64, z);
                    if (biomeObj != null) {
                        String biomeName = String.valueOf(biomeObj).toUpperCase(Locale.ROOT);
                        if (biomeName.contains("OCEAN")) {
                            return false;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return true;
    }

    private World createFilteredManhuntWorld(CommandSender notifyTarget) {
        World previousCustomWorld = (activeOverworld != null && activeOverworld.getName().startsWith("manhunt_world_"))
                ? activeOverworld
                : null;

        World fallbackWorld = Bukkit.getWorlds().get(0);
        Location tempSafeLoc = fallbackWorld.getSpawnLocation();

        if (previousCustomWorld != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld().equals(previousCustomWorld)) {
                    p.teleport(tempSafeLoc);
                }
            }
            unloadAndDeleteWorld(previousCustomWorld);
        }

        World acceptedWorld = null;
        int maxAttempts = 15;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            long candidateSeed = (attempt <= maxAttempts - 2)
                    ? random.nextLong()
                    : FALLBACK_LAND_SEEDS[random.nextInt(FALLBACK_LAND_SEEDS.length)];

            String worldName = "manhunt_world_" + System.currentTimeMillis() + "_" + attempt;
            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(World.Environment.NORMAL);
            creator.type(WorldType.NORMAL);
            creator.seed(candidateSeed);
            creator.keepSpawnInMemory(false);

            World candidate = Bukkit.createWorld(creator);
            if (candidate == null) continue;

            if (isWorldOceanFreeInRadius200(candidate)) {
                acceptedWorld = candidate;
                if (notifyTarget != null) {
                    notifyTarget.sendMessage(ChatColor.GREEN + "[Manhunt] Найден подходящий сид без океана в радиусе 200 блоков (попытка #" + attempt + ")!");
                }
                break;
            } else {
                unloadAndDeleteWorld(candidate);
            }
        }

        if (acceptedWorld == null) {
            acceptedWorld = fallbackWorld;
        }

        activeOverworld = acceptedWorld;
        return acceptedWorld;
    }

    private void unloadAndDeleteWorld(World world) {
        if (world == null) return;
        File worldFolder = world.getWorldFolder();
        try {
            Bukkit.unloadWorld(world, false);
        } catch (Throwable ignored) {
        }
        deleteDirectoryRecursive(worldFolder);
    }

    private void deleteDirectoryRecursive(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirectoryRecursive(f);
                } else {
                    f.delete();
                }
            }
        }
        dir.delete();
    }

    private void handleSpRestart(Player speedrunner) {
        if (gameState != GameState.PICKING_PLACE) {
            speedrunner.sendMessage(ChatColor.RED + "Команда /sp restart доступна только во время выбора места старта (после /sp pickplace start)!");
            return;
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "[Manhunt] " + ChatColor.RED + speedrunner.getName()
                + ChatColor.YELLOW + " запустил пересоздание мира (/sp restart). Ищем новый сид без океана...");

        waitingBoxBuilt = false;
        World newWorld = createFilteredManhuntWorld(speedrunner);

        Location boxSpawn = buildWaitingBox(newWorld);
        applyPreparationWorldRules(newWorld, boxSpawn);

        WorldBorder border = newWorld.getWorldBorder();
        border.setCenter(0.0, 0.0);
        border.setSize(selectedDifficulty.getPickPlaceBorderSize());
        dispatchWorldBorderCommand(newWorld, "center 0 0");
        dispatchWorldBorderCommand(newWorld, "set " + (int) selectedDifficulty.getPickPlaceBorderSize());

        int highestY = getHighestSurfaceYIgnoringBarriers(newWorld, 0, 0);
        Location centerLoc = new Location(newWorld, 0.5, highestY + 10.0, 0.5);

        speedrunner.setGameMode(GameMode.SPECTATOR);
        speedrunner.setAllowFlight(true);
        speedrunner.setFlying(true);
        speedrunner.teleport(centerLoc);

        updateSidebarScoreboard();

        Bukkit.broadcastMessage(ChatColor.GOLD + "[Manhunt] " + ChatColor.GREEN
                + "Новый мир успешно создан (Сид: " + newWorld.getSeed() + ")! Спидранер продолжает выбор места старта.");
    }

    // =========================================================================
    // ОБРАБОТКА КОМАНД
    // =========================================================================

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmdName = command.getName().toLowerCase(Locale.ROOT);

        try {
            switch (cmdName) {
                case "manhunt":
                    return handleManhuntCommand(sender, args);
                case "sp":
                    return handleSpCommand(sender, args);
                case "ready":
                    return handleReadyCommand(sender, true);
                case "unready":
                    return handleReadyCommand(sender, false);
                case "fortrest":
                    return handleFortrestCommand(sender);
                case "pause":
                    return handlePauseCommand(sender, args);
                case "compass":
                    return handleCompassCommand(sender);
                case "stop":
                    return handleLongStopCommand(sender);
                case "resume":
                    return handleResumeCommand(sender);
                default:
                    return false;
            }
        } catch (Throwable t) {
            sender.sendMessage(ChatColor.RED + "Ошибка при выполнении команды: " + t.getClass().getSimpleName() + ": " + t.getMessage());
            getLogger().severe("Ошибка при выполнении команды /" + label + ":");
            t.printStackTrace();
            return true;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().trim().toLowerCase(Locale.ROOT);
        if (msg.equals("/stop") || msg.startsWith("/stop ")) {
            if (gameState == GameState.IN_GAME) {
                event.setCancelled(true);
                handleLongStopCommand(event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event) {
        String cmd = event.getCommand().trim().toLowerCase(Locale.ROOT);
        if (cmd.equals("stop") && gameState == GameState.IN_GAME && !longTermStopped) {
            event.setCancelled(true);
            handleLongStopCommand(event.getSender());
        }
    }

    private boolean handleManhuntCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendManhuntHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("info")) {
            sendFullModInfo(sender);
            return true;
        }

        if (sub.equals("difficulty")) {
            if (args.length >= 2 && args[1].equalsIgnoreCase("info")) {
                sendDifficultyInfo(sender);
                return true;
            }
            sender.sendMessage(ChatColor.RED + "Использование: /manhunt difficulty info");
            return true;
        }

        if (!sender.isOp() && !sender.hasPermission("manhunt.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав администратора для выполнения этой команды.");
            return true;
        }

        if (sub.equals("game")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Использование: /manhunt game <start_prepare|start|reset|end>");
                return true;
            }
            String action = args[1].toLowerCase(Locale.ROOT);
            switch (action) {
                case "start_prepare":
                case "start":
                    startPreparePhase(sender);
                    return true;
                case "reset":
                    resetGameFull(sender);
                    return true;
                case "end":
                    endGameCleanup(sender);
                    return true;
                default:
                    sender.sendMessage(ChatColor.RED + "Неизвестное действие. Используйте: start_prepare (или start), reset или end.");
                    return true;
            }
        }

        if (sub.equals("speedruner") || sub.equals("speedrunner")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Использование: /manhunt speedruner <add|remove> <Игрок>");
                return true;
            }
            if (!isPreparationStage()) {
                sender.sendMessage(ChatColor.RED + "Менять спидранера можно только на этапе подготовки (после /manhunt game start_prepare и до начала игры).");
                return true;
            }
            String action = args[1].toLowerCase(Locale.ROOT);
            Player target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Игрок " + args[2] + " не найден на сервере.");
                return true;
            }

            if (action.equals("add")) {
                addSpeedrunner(sender, target);
            } else if (action.equals("remove")) {
                removeSpeedrunner(sender, target);
            } else {
                sender.sendMessage(ChatColor.RED + "Использование: /manhunt speedruner <add|remove> <Игрок>");
            }
            return true;
        }

        if (sub.equals("hunter")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Использование: /manhunt hunter <add|remove> <Игрок>");
                return true;
            }
            if (!isPreparationStage()) {
                sender.sendMessage(ChatColor.RED + "Менять охотников можно только на этапе подготовки (до начала игры).");
                return true;
            }
            String action = args[1].toLowerCase(Locale.ROOT);
            Player target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Игрок " + args[2] + " не найден на сервере.");
                return true;
            }

            if (action.equals("add")) {
                addHunter(sender, target);
            } else if (action.equals("remove")) {
                removeHunter(sender, target);
            } else {
                sender.sendMessage(ChatColor.RED + "Использование: /manhunt hunter <add|remove> <Игрок>");
            }
            return true;
        }

        sendManhuntHelp(sender);
        return true;
    }

    private boolean handleSpCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эту команду может использовать только игрок-спидранер.");
            return true;
        }
        Player player = (Player) sender;

        if (speedrunnerUuid == null || !speedrunnerUuid.equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Эта команда доступна только назначенному спидранеру!");
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("restart")) {
            handleSpRestart(player);
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Доступные команды спидранера (в строгой последовательности):");
            player.sendMessage(ChatColor.GRAY + "1. /sp difficulty select <easy|medium|hard|impossible>");
            player.sendMessage(ChatColor.GRAY + "2. /sp pickplace start");
            player.sendMessage(ChatColor.GRAY + "   • /sp restart — пересоздать мир с новым сидом во время выбора места");
            player.sendMessage(ChatColor.GRAY + "3. /sp pickplace end");
            return true;
        }

        String category = args[0].toLowerCase(Locale.ROOT);
        String action = args[1].toLowerCase(Locale.ROOT);

        // Шаг 1: /sp difficulty select <mode>
        if (category.equals("difficulty") && action.equals("select")) {
            if (gameState != GameState.PREPARE && gameState != GameState.DIFFICULTY_SELECTED) {
                player.sendMessage(ChatColor.RED + "Сейчас нельзя выбрать сложность. Требуется этап подготовки (Шаг 1).");
                return true;
            }
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Использование: /sp difficulty select <easy|medium|hard|impossible>");
                return true;
            }
            DifficultyMode mode = DifficultyMode.fromString(args[2]);
            if (mode == null) {
                player.sendMessage(ChatColor.RED + "Неверная сложность! Доступны: easy, medium, hard, impossible.");
                return true;
            }

            selectedDifficulty = mode;
            gameState = GameState.DIFFICULTY_SELECTED;
            updateSidebarScoreboard();

            Bukkit.broadcastMessage(ChatColor.GOLD + "[Manhunt] " + ChatColor.RED + player.getName()
                    + ChatColor.YELLOW + " выбрал сложность: " + mode.getColoredDisplayName());
            player.sendMessage(ChatColor.GREEN + "Сложность выбрана! Следующий шаг: "
                    + ChatColor.YELLOW + "/sp pickplace start");
            return true;
        }

        // Шаг 2: /sp pickplace start
        if (category.equals("pickplace") && action.equals("start")) {
            if (gameState != GameState.DIFFICULTY_SELECTED) {
                player.sendMessage(ChatColor.RED + "Сначала необходимо выбрать сложность командой: "
                        + ChatColor.YELLOW + "/sp difficulty select <easy|medium|hard|impossible>");
                return true;
            }
            startPickPlace(player);
            return true;
        }

        // Шаг 3: /sp pickplace end
        if (category.equals("pickplace") && action.equals("end")) {
            if (gameState != GameState.PICKING_PLACE) {
                player.sendMessage(ChatColor.RED + "Сначала начните выбор места командой: "
                        + ChatColor.YELLOW + "/sp pickplace start");
                return true;
            }
            endPickPlace(player);
            return true;
        }

        player.sendMessage(ChatColor.RED + "Неизвестная подкоманда /sp.");
        return true;
    }

    private boolean handleReadyCommand(CommandSender sender, boolean readyState) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Команда доступна только игрокам.");
            return true;
        }
        Player player = (Player) sender;

        if (gameState != GameState.WAITING_READY && gameState != GameState.COUNTDOWN) {
            player.sendMessage(ChatColor.RED + "Команды /ready и /unready сейчас недоступны. Ожидайте завершения выбора места старта спидранером.");
            return true;
        }

        UUID uuid = player.getUniqueId();
        if (readyState) {
            if (readyPlayers.contains(uuid)) {
                player.sendMessage(ChatColor.YELLOW + "Вы уже находитесь в режиме готовности.");
                return true;
            }
            readyPlayers.add(uuid);
            player.setGlowing(true);
            Bukkit.broadcastMessage(ChatColor.GREEN + "[Manhunt] Игрок " + player.getDisplayName()
                    + ChatColor.GREEN + " готов к игре! (" + readyPlayers.size() + "/" + getRequiredPlayersCount() + ")");
            checkAllPlayersReady();
        } else {
            if (!readyPlayers.contains(uuid)) {
                player.sendMessage(ChatColor.YELLOW + "Вы ещё не в режиме готовности.");
                return true;
            }
            readyPlayers.remove(uuid);
            player.setGlowing(false);
            Bukkit.broadcastMessage(ChatColor.RED + "[Manhunt] Игрок " + player.getDisplayName()
                    + ChatColor.RED + " отменил готовность. (" + readyPlayers.size() + "/" + getRequiredPlayersCount() + ")");
            if (gameState == GameState.COUNTDOWN) {
                cancelCountdownAndRestoreBorder();
            }
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    private boolean handleFortrestCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Команда доступна только игроку.");
            return true;
        }
        Player player = (Player) sender;

        if (gameState != GameState.IN_GAME) {
            player.sendMessage(ChatColor.RED + "Команда /fortrest доступна только во время активной игры!");
            return true;
        }
        if (speedrunnerUuid == null || !speedrunnerUuid.equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Команда /fortrest доступна только спидранеру!");
            return true;
        }
        if (selectedDifficulty != DifficultyMode.EASY) {
            player.sendMessage(ChatColor.RED + "Команда /fortrest доступна только на уровне сложности EASY!");
            return true;
        }

        World world = player.getWorld();
        Location origin = player.getLocation();

        if (world.getEnvironment() != World.Environment.NETHER) {
            World nether = getNetherWorld();
            if (nether != null) {
                world = nether;
                origin = new Location(nether, origin.getX() / 8.0, origin.getY(), origin.getZ() / 8.0);
            }
        }

        Location fortressLoc = null;
        try {
            fortressLoc = world.locateNearestStructure(origin, StructureType.NETHER_FORTRESS, 100, false);
        } catch (Throwable ignored) {
        }

        if (fortressLoc == null) {
            player.sendMessage(ChatColor.RED + "[Manhunt] Не удалось найти ближайшую крепость Незера поблизости.");
        } else {
            player.sendMessage(ChatColor.GOLD + "[Manhunt] " + ChatColor.YELLOW
                    + "Координаты ближайшей крепости Ада (Nether Fortress): "
                    + ChatColor.GREEN + "X: " + fortressLoc.getBlockX()
                    + ", Z: " + fortressLoc.getBlockZ()
                    + ChatColor.GRAY + " (в измерении Незера)");
        }
        return true;
    }

    // =========================================================================
    // КОМАНДА /compass (/c) ДЛЯ ОХОТНИКОВ
    // =========================================================================

    private boolean handleCompassCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Команду может использовать только игрок.");
            return true;
        }
        Player player = (Player) sender;

        if (gameState != GameState.IN_GAME) {
            player.sendMessage(ChatColor.RED + "Команда /compass доступна только во время активной игры!");
            return true;
        }

        if (!hunterUuids.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Эта команда доступна только охотникам!");
            return true;
        }

        if (hasHunterCompass(player)) {
            player.sendMessage(ChatColor.YELLOW + "Вы уже имеете компас");
            return true;
        }

        player.getInventory().addItem(createHunterCompass());
        Player runner = speedrunnerUuid != null ? Bukkit.getPlayer(speedrunnerUuid) : null;
        updateAllHunterCompasses(runner);
        player.sendMessage(ChatColor.GREEN + "Вам выдан Компас Охотника!");
        return true;
    }

    // =========================================================================
    // ДЛИТЕЛЬНАЯ ОСТАНОВКА ИГРЫ (/stop И /resume) С СОХРАНЕНИЕМ НА ДИСК
    // =========================================================================

    private boolean handleLongStopCommand(CommandSender sender) {
        if (gameState != GameState.IN_GAME) {
            sender.sendMessage(ChatColor.RED + "Команда /stop для заморозки Manhunt доступна только во время активной игры!");
            return true;
        }
        if (longTermStopped) {
            sender.sendMessage(ChatColor.YELLOW + "Игра уже остановлена! Для продолжения введите: /resume");
            return true;
        }

        if (pauseTask != null) {
            pauseTask.cancel();
            pauseTask = null;
        }
        if (pauseBossBar != null) {
            pauseBossBar.removeAll();
            pauseBossBar = null;
        }
        prePauseActive = false;

        if (!gamePaused) {
            applyFreezeState(3600 * 24);
        }
        gamePaused = true;
        longTermStopped = true;

        frozenLocationsDuringStop.clear();
        for (Player p : Bukkit.getOnlinePlayers()) {
            frozenLocationsDuringStop.put(p.getUniqueId(), p.getLocation().clone());
        }

        if (stopBossBar != null) {
            stopBossBar.removeAll();
        }
        stopBossBar = Bukkit.createBossBar(
                ChatColor.RED + "" + ChatColor.BOLD + "ИГРА ОСТАНОВЛЕНА (/stop) "
                        + ChatColor.GRAY + "— " + ChatColor.YELLOW + "Для возобновления пропишите /resume",
                BarColor.RED,
                BarStyle.SOLID
        );
        stopBossBar.setProgress(1.0);
        for (Player p : Bukkit.getOnlinePlayers()) {
            stopBossBar.addPlayer(p);
        }

        saveGameStateToDisk();

        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");
        Bukkit.broadcastMessage(ChatColor.RED + "Игра Manhunt поставлена на длительную паузу (/stop)!");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Все позиции, роли, эффекты и компасы сохранены.");
        Bukkit.broadcastMessage(ChatColor.GREEN + "Чтобы возобновить игру в любой момент, пропишите: /resume");
        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");
        return true;
    }

    private boolean handleResumeCommand(CommandSender sender) {
        if (!longTermStopped) {
            sender.sendMessage(ChatColor.RED + "Игра сейчас не остановлена командой /stop.");
            return true;
        }

        longTermStopped = false;
        frozenLocationsDuringStop.clear();
        if (stopBossBar != null) {
            stopBossBar.removeAll();
            stopBossBar = null;
        }

        stopPauseImmediately(true);
        clearSavedGameStateFile();
        disableLocatorBarEverywhere();

        Bukkit.broadcastMessage(ChatColor.GOLD + "[Manhunt] " + ChatColor.GREEN
                + "Игра возобновлена командой /resume! Все таймеры и компасы продолжают работу.");
        for (Player p : Bukkit.getOnlinePlayers()) {
            restorePlayerMovement(p);
            p.sendTitle(ChatColor.GREEN + "Игра возобновлена!", "", 5, 30, 10);
            playSoundSafe(p, SOUND_LEVELUP, 1.0f, 1.2f);
        }
        return true;
    }

    private void saveGameStateToDisk() {
        try {
            YamlConfiguration cfg = new YamlConfiguration();
            cfg.set("active", true);
            cfg.set("gameState", gameState.name());
            cfg.set("longTermStopped", longTermStopped);
            cfg.set("selectedDifficulty", selectedDifficulty != null ? selectedDifficulty.name() : null);
            cfg.set("speedrunnerUuid", speedrunnerUuid != null ? speedrunnerUuid.toString() : null);
            cfg.set("hunterUuids", hunterUuids.stream().map(UUID::toString).collect(Collectors.toList()));
            cfg.set("elapsedGameSeconds", elapsedGameSeconds);
            cfg.set("activeOverworld", activeOverworld != null ? activeOverworld.getName() : null);

            if (chosenStartLocation != null && chosenStartLocation.getWorld() != null) {
                cfg.set("chosenStart.world", chosenStartLocation.getWorld().getName());
                cfg.set("chosenStart.x", chosenStartLocation.getX());
                cfg.set("chosenStart.y", chosenStartLocation.getY());
                cfg.set("chosenStart.z", chosenStartLocation.getZ());
            }

            for (Map.Entry<World.Environment, Location> entry : lastRunnerLocations.entrySet()) {
                Location loc = entry.getValue();
                if (loc != null && loc.getWorld() != null) {
                    String path = "lastRunnerLocations." + entry.getKey().name();
                    cfg.set(path + ".world", loc.getWorld().getName());
                    cfg.set(path + ".x", loc.getX());
                    cfg.set(path + ".y", loc.getY());
                    cfg.set(path + ".z", loc.getZ());
                }
            }

            for (Map.Entry<UUID, Location> entry : frozenLocationsDuringStop.entrySet()) {
                Location loc = entry.getValue();
                if (loc != null && loc.getWorld() != null) {
                    String path = "frozenLocations." + entry.getKey().toString();
                    cfg.set(path + ".world", loc.getWorld().getName());
                    cfg.set(path + ".x", loc.getX());
                    cfg.set(path + ".y", loc.getY());
                    cfg.set(path + ".z", loc.getZ());
                    cfg.set(path + ".yaw", (double) loc.getYaw());
                    cfg.set(path + ".pitch", (double) loc.getPitch());
                }
            }

            cfg.set("overworldPortals", runnerOverworldPortalBlocks.stream().map(BlockPos::serialize).collect(Collectors.toList()));
            cfg.set("netherPortals", runnerInitialNetherPortalBlocks.stream().map(BlockPos::serialize).collect(Collectors.toList()));

            cfg.save(savedStateFile);
        } catch (Throwable t) {
            getLogger().warning("Не удалось сохранить состояние игры на диск: " + t.getMessage());
        }
    }

    private void loadSavedGameStateIfPresent() {
        if (savedStateFile == null || !savedStateFile.exists()) return;
        try {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(savedStateFile);
            if (!cfg.getBoolean("active", false)) return;

            String worldName = cfg.getString("activeOverworld");
            if (worldName != null && !worldName.isEmpty()) {
                World w = Bukkit.getWorld(worldName);
                if (w == null && new File(Bukkit.getWorldContainer(), worldName).exists()) {
                    w = Bukkit.createWorld(new WorldCreator(worldName));
                }
                if (w != null) {
                    activeOverworld = w;
                }
            }

            this.gameState = GameState.valueOf(cfg.getString("gameState", "IN_GAME"));
            this.longTermStopped = cfg.getBoolean("longTermStopped", true);
            this.gamePaused = this.longTermStopped;
            this.scoreboardActive = true;

            String diffStr = cfg.getString("selectedDifficulty");
            if (diffStr != null) {
                this.selectedDifficulty = DifficultyMode.valueOf(diffStr);
            }

            String srStr = cfg.getString("speedrunnerUuid");
            if (srStr != null && !srStr.isEmpty()) {
                this.speedrunnerUuid = UUID.fromString(srStr);
            }

            this.hunterUuids.clear();
            for (String h : cfg.getStringList("hunterUuids")) {
                this.hunterUuids.add(UUID.fromString(h));
            }

            this.elapsedGameSeconds = cfg.getLong("elapsedGameSeconds", 0L);

            if (cfg.contains("chosenStart.world")) {
                World cw = Bukkit.getWorld(cfg.getString("chosenStart.world", "world"));
                if (cw != null) {
                    this.chosenStartLocation = new Location(
                            cw,
                            cfg.getDouble("chosenStart.x"),
                            cfg.getDouble("chosenStart.y"),
                            cfg.getDouble("chosenStart.z")
                    );
                }
            }

            ConfigurationSection lastLocSec = cfg.getConfigurationSection("lastRunnerLocations");
            if (lastLocSec != null) {
                for (String envKey : lastLocSec.getKeys(false)) {
                    World lw = Bukkit.getWorld(lastLocSec.getString(envKey + ".world", "world"));
                    if (lw != null) {
                        Location loc = new Location(
                                lw,
                                lastLocSec.getDouble(envKey + ".x"),
                                lastLocSec.getDouble(envKey + ".y"),
                                lastLocSec.getDouble(envKey + ".z")
                        );
                        lastRunnerLocations.put(World.Environment.valueOf(envKey), loc);
                    }
                }
            }

            ConfigurationSection frozenSec = cfg.getConfigurationSection("frozenLocations");
            if (frozenSec != null) {
                for (String uuidStr : frozenSec.getKeys(false)) {
                    World fw = Bukkit.getWorld(frozenSec.getString(uuidStr + ".world", "world"));
                    if (fw != null) {
                        Location loc = new Location(
                                fw,
                                frozenSec.getDouble(uuidStr + ".x"),
                                frozenSec.getDouble(uuidStr + ".y"),
                                frozenSec.getDouble(uuidStr + ".z"),
                                (float) frozenSec.getDouble(uuidStr + ".yaw"),
                                (float) frozenSec.getDouble(uuidStr + ".pitch")
                        );
                        frozenLocationsDuringStop.put(UUID.fromString(uuidStr), loc);
                    }
                }
            }

            runnerOverworldPortalBlocks.clear();
            for (String s : cfg.getStringList("overworldPortals")) {
                BlockPos bp = BlockPos.deserialize(s);
                if (bp != null) runnerOverworldPortalBlocks.add(bp);
            }
            runnerInitialNetherPortalBlocks.clear();
            for (String s : cfg.getStringList("netherPortals")) {
                BlockPos bp = BlockPos.deserialize(s);
                if (bp != null) runnerInitialNetherPortalBlocks.add(bp);
            }

            if (longTermStopped) {
                dispatchConsoleCommandSafe("tick freeze");
                stopBossBar = Bukkit.createBossBar(
                        ChatColor.RED + "" + ChatColor.BOLD + "ИГРА ОСТАНОВЛЕНА (/stop) "
                                + ChatColor.GRAY + "— " + ChatColor.YELLOW + "Для возобновления пропишите /resume",
                        BarColor.RED,
                        BarStyle.SOLID
                );
                stopBossBar.setProgress(1.0);
            }

            disableLocatorBarEverywhere();
            updateSidebarScoreboard();
            getLogger().info("Состояние игры Manhunt восстановлено из saved_state.yml!");
        } catch (Throwable t) {
            getLogger().warning("Не удалось загрузить saved_state.yml: " + t.getMessage());
        }
    }

    private void clearSavedGameStateFile() {
        if (savedStateFile != null && savedStateFile.exists()) {
            savedStateFile.delete();
        }
    }

    // =========================================================================
    // МЕХАНИКА ПАУЗЫ (/pause и /p) И ИСПРАВЛЕНИЕ ПЕРЕЗАХОДА ВО ВРЕМЯ ПАУЗЫ
    // =========================================================================

    private boolean handlePauseCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Команду /pause может использовать только игрок.");
            return true;
        }
        Player player = (Player) sender;

        if (gameState != GameState.IN_GAME) {
            player.sendMessage(ChatColor.RED + "Команду /pause можно использовать только во время активной игры!");
            return true;
        }

        if (prePauseActive || gamePaused || longTermStopped) {
            player.sendMessage(ChatColor.RED + "Пауза уже активна или готовится к запуску!");
            return true;
        }

        long now = System.currentTimeMillis();

        if (now < pauseCooldownEndMillis) {
            long remainingSeconds = Math.max(1L, (pauseCooldownEndMillis - now + 999L) / 1000L);
            player.sendMessage(ChatColor.RED + "Команда станет снова доступна через " + formatCooldownTime(remainingSeconds) + ".");
            return true;
        }

        if (now < combatCooldownEndMillis) {
            long remainingCombatSeconds = Math.max(1L, (combatCooldownEndMillis - now + 999L) / 1000L);
            player.sendMessage(ChatColor.RED + "Команда будет доступна через "
                    + formatCooldownTime(remainingCombatSeconds) + " из-за недавней схватки");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Использование: /pause <время в секундах (максимум 180)>");
            return true;
        }

        int pauseDuration;
        try {
            pauseDuration = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Укажите корректное число секунд (от 1 до 180)!");
            return true;
        }

        if (pauseDuration <= 0 || pauseDuration > MAX_PAUSE_SECONDS) {
            player.sendMessage(ChatColor.RED + "Время паузы должно быть от 1 до " + MAX_PAUSE_SECONDS + " секунд!");
            return true;
        }

        startPauseSequence(player.getName(), pauseDuration);
        return true;
    }

    private void startPauseSequence(String initiatorName, int pauseSeconds) {
        stopPauseImmediately(false);
        prePauseActive = true;

        pauseBossBar = Bukkit.createBossBar(
                ChatColor.YELLOW + "Через 10 сек. игра будет заморожена на " + pauseSeconds + " сек.",
                BarColor.YELLOW,
                BarStyle.SOLID
        );
        pauseBossBar.setProgress(1.0);
        for (Player p : Bukkit.getOnlinePlayers()) {
            pauseBossBar.addPlayer(p);
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "[Manhunt] " + ChatColor.AQUA + initiatorName
                + ChatColor.YELLOW + " запросил паузу на " + ChatColor.GREEN + pauseSeconds + " сек."
                + ChatColor.YELLOW + " Заморозка через 10 секунд!");

        pauseTask = new BukkitRunnable() {
            int preCountdown = 10;
            int remainingPause = pauseSeconds;
            boolean inFreezePhase = false;

            @Override
            public void run() {
                if (gameState != GameState.IN_GAME || longTermStopped) {
                    if (!longTermStopped) {
                        stopPauseImmediately(true);
                    }
                    return;
                }

                if (!inFreezePhase) {
                    if (preCountdown > 0) {
                        if (pauseBossBar != null) {
                            pauseBossBar.setTitle(ChatColor.YELLOW + "Через " + preCountdown
                                    + " сек. игра будет заморожена на " + pauseSeconds + " сек.");
                            pauseBossBar.setProgress(Math.max(0.0, Math.min(1.0, preCountdown / 10.0)));
                        }
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            playSoundSafe(p, SOUND_ELECTRIC_PIANO, 1.0f, 2.0f);
                        }
                        preCountdown--;
                        return;
                    }

                    inFreezePhase = true;
                    prePauseActive = false;
                    gamePaused = true;
                    applyFreezeState(pauseSeconds);
                }

                if (remainingPause > 0) {
                    if (pauseBossBar != null) {
                        pauseBossBar.setColor(BarColor.BLUE);
                        pauseBossBar.setTitle(ChatColor.AQUA + initiatorName + ChatColor.WHITE
                                + " начал перерыв. Окончание через " + ChatColor.YELLOW + remainingPause + " сек.");
                        pauseBossBar.setProgress(Math.max(0.0, Math.min(1.0, remainingPause / (double) pauseSeconds)));
                    }

                    if (remainingPause <= 5) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendTitle(ChatColor.AQUA + String.valueOf(remainingPause),
                                    ChatColor.YELLOW + "До возобновления игры...", 0, 25, 5);
                            playSoundSafe(p, SOUND_ELECTRIC_PIANO, 1.0f, 1.5f);
                        }
                    }

                    remainingPause--;
                } else {
                    finishPauseNormally();
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private void applyFreezeState(int pauseSeconds) {
        dispatchConsoleCommandSafe("tick freeze");

        savedEffectsDuringPause.clear();
        PotionEffectType slowType = resolvePotionEffect("slowness", "SLOWNESS", "SLOW");
        int ticks = (pauseSeconds + 5) * 20;

        for (Player p : Bukkit.getOnlinePlayers()) {
            List<PotionEffect> copy = new ArrayList<>(p.getActivePotionEffects());
            savedEffectsDuringPause.put(p.getUniqueId(), copy);
            for (PotionEffect eff : copy) {
                p.removePotionEffect(eff.getType());
            }
            if (slowType != null) {
                p.addPotionEffect(new PotionEffect(slowType, ticks, 250, false, false, false));
            }
            p.setWalkSpeed(0.0f);
            p.setFlySpeed(0.0f);
        }
    }

    private void finishPauseNormally() {
        stopPauseImmediately(true);
        pauseCooldownEndMillis = System.currentTimeMillis() + PAUSE_COOLDOWN_MILLIS;

        Bukkit.broadcastMessage(ChatColor.GOLD + "[Manhunt] " + ChatColor.GREEN
                + "Пауза окончена! Игра продолжается. Следующая пауза будет доступна через 10 минут.");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(ChatColor.GREEN + "Игра продолжается!", "", 5, 30, 10);
            playSoundSafe(p, SOUND_LEVELUP, 1.0f, 1.2f);
        }
    }

    private void stopPauseImmediately(boolean unfreezeWorld) {
        if (pauseTask != null) {
            pauseTask.cancel();
            pauseTask = null;
        }
        if (pauseBossBar != null) {
            pauseBossBar.removeAll();
            pauseBossBar = null;
        }

        boolean wasPaused = gamePaused;
        prePauseActive = false;
        gamePaused = false;

        if (wasPaused || unfreezeWorld) {
            dispatchConsoleCommandSafe("tick unfreeze");

            for (Player p : Bukkit.getOnlinePlayers()) {
                restorePlayerMovement(p);
            }
            savedEffectsDuringPause.clear();
        }
    }

    private void restorePlayerMovement(Player p) {
        if (p == null) return;
        try {
            p.setWalkSpeed(0.2f);
            p.setFlySpeed(0.1f);
            PotionEffectType slowType = resolvePotionEffect("slowness", "SLOWNESS", "SLOW");
            if (slowType != null) {
                PotionEffect activeSlow = p.getPotionEffect(slowType);
                if (activeSlow != null && activeSlow.getAmplifier() >= 10) {
                    p.removePotionEffect(slowType);
                }
            }
            Collection<PotionEffect> saved = savedEffectsDuringPause.remove(p.getUniqueId());
            if (saved != null) {
                for (PotionEffect eff : saved) {
                    p.addPotionEffect(eff);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private String formatCooldownTime(long totalSeconds) {
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        if (minutes > 0) {
            return minutes + " мин. " + seconds + " сек";
        }
        return seconds + " сек";
    }

    // =========================================================================
    // 10-СЕКУНДНЫЙ ПОВТОР СМЕРТИ СПИДРАНЕРА (DEATH REPLAY / KILLCAM)
    // =========================================================================

    private void startReplayRecordingTask() {
        replayRecordingTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (gameState != GameState.IN_GAME || gamePaused || longTermStopped) {
                    pendingBlockChangesThisTick.clear();
                    swungHandThisTick.clear();
                    hurtThisTick.clear();
                    return;
                }

                Player runner = speedrunnerUuid != null ? Bukkit.getPlayer(speedrunnerUuid) : null;
                if (runner == null || !runner.isOnline() || runner.isDead()) {
                    return;
                }

                Location center = runner.getLocation();
                World world = center.getWorld();
                if (world == null) return;

                ReplayFrame frame = new ReplayFrame();

                for (Player p : world.getPlayers()) {
                    if (p.getLocation().distanceSquared(center) <= REPLAY_CAPTURE_RADIUS * REPLAY_CAPTURE_RADIUS) {
                        boolean swung = swungHandThisTick.contains(p.getUniqueId());
                        boolean hurt = hurtThisTick.contains(p.getUniqueId());
                        frame.players.add(new PlayerSnapshot(p, swung, hurt));
                    }
                }

                for (Entity ent : world.getNearbyEntities(center, REPLAY_CAPTURE_RADIUS, REPLAY_CAPTURE_RADIUS, REPLAY_CAPTURE_RADIUS)) {
                    if (ent instanceof Player || ent instanceof ArmorStand) continue;
                    frame.entities.add(new EntitySnapshot(ent));
                }

                if (!pendingBlockChangesThisTick.isEmpty()) {
                    frame.blockChanges.addAll(pendingBlockChangesThisTick);
                    pendingBlockChangesThisTick.clear();
                }

                swungHandThisTick.clear();
                hurtThisTick.clear();

                replayFrames.addLast(frame);
                while (replayFrames.size() > REPLAY_BUFFER_TICKS) {
                    replayFrames.removeFirst();
                }
            }
        }.runTaskTimer(this, 1L, 1L);
    }

    private void recordBlockChange(Block block, BlockData beforeData, BlockData afterData) {
        if (gameState != GameState.IN_GAME || gamePaused || longTermStopped || block == null) return;
        Player runner = speedrunnerUuid != null ? Bukkit.getPlayer(speedrunnerUuid) : null;
        if (runner != null && runner.getWorld().equals(block.getWorld())) {
            if (runner.getLocation().distanceSquared(block.getLocation()) <= 40.0 * 40.0) {
                pendingBlockChangesThisTick.add(new BlockChangeRecord(BlockPos.of(block), beforeData, afterData));
            }
        }
    }

    private void startDeathReplayAndThenHuntersWin(Location deathLocation) {
        if (gameState == GameState.REPLAYING_DEATH || gameState == GameState.ENDED) return;
        stopPauseImmediately(false);

        if (replayFrames.isEmpty() || deathLocation == null || deathLocation.getWorld() == null) {
            triggerHuntersWinFinal();
            return;
        }

        gameState = GameState.REPLAYING_DEATH;
        final List<ReplayFrame> framesToPlay = new ArrayList<>(replayFrames);
        replayFrames.clear();

        // Откатываем все записанные блоки к состоянию 10 секунд назад
        for (int i = framesToPlay.size() - 1; i >= 0; i--) {
            ReplayFrame f = framesToPlay.get(i);
            for (int j = f.blockChanges.size() - 1; j >= 0; j--) {
                BlockChangeRecord rec = f.blockChanges.get(j);
                Block b = rec.pos.toBlock();
                if (b != null && rec.beforeData != null) {
                    b.setBlockData(rec.beforeData, false);
                }
            }
        }

        Location spectatorViewLoc = deathLocation.clone().add(0.0, 2.5, 0.0);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGlowing(false);
            p.setGameMode(GameMode.SPECTATOR);
            p.teleport(spectatorViewLoc);
            p.sendTitle(ChatColor.RED + "ПОВТОР СМЕРТИ", ChatColor.YELLOW + "Последние 10 секунд перед гибелью спидранера", 5, 45, 10);
        }
        Bukkit.broadcastMessage(ChatColor.GOLD + "[Manhunt] " + ChatColor.AQUA
                + "Воспроизведение повтора последних 10 секунд перед смертью спидранера...");

        cleanupReplayEntities();

        replayPlaybackTask = new BukkitRunnable() {
            int frameIndex = 0;

            @Override
            public void run() {
                if (frameIndex >= framesToPlay.size() || gameState != GameState.REPLAYING_DEATH) {
                    this.cancel();
                    replayPlaybackTask = null;
                    cleanupReplayEntities();
                    triggerHuntersWinFinal();
                    return;
                }

                ReplayFrame frame = framesToPlay.get(frameIndex++);
                renderReplayFrame(frame);
            }
        }.runTaskTimer(this, 1L, 1L);
    }

    private void renderReplayFrame(ReplayFrame frame) {
        for (BlockChangeRecord rec : frame.blockChanges) {
            Block b = rec.pos.toBlock();
            if (b != null && rec.afterData != null) {
                b.setBlockData(rec.afterData, false);
            }
        }

        Set<UUID> activePlayersInFrame = new HashSet<>();
        for (PlayerSnapshot snap : frame.players) {
            activePlayersInFrame.add(snap.uuid);
            ArmorStand stand = replayPlayerClones.get(snap.uuid);
            if (stand == null || !stand.isValid()) {
                World w = snap.location.getWorld();
                if (w == null) continue;
                stand = w.spawn(snap.location, ArmorStand.class);
                stand.setBasePlate(false);
                stand.setArms(true);
                stand.setGravity(false);
                stand.setInvulnerable(true);
                stand.setCustomName(snap.displayName);
                stand.setCustomNameVisible(true);
                replayPlayerClones.put(snap.uuid, stand);
            } else {
                stand.teleport(snap.location);
            }

            stand.setHeadPose(new EulerAngle(Math.toRadians(snap.location.getPitch()), 0, 0));
            if (snap.swungHand) {
                stand.setRightArmPose(new EulerAngle(Math.toRadians(-75), 0, 0));
            } else {
                stand.setRightArmPose(new EulerAngle(Math.toRadians(-15), 0, 0));
            }

            EntityEquipment eq = stand.getEquipment();
            if (eq != null) {
                if (snap.helmet != null && snap.helmet.getType() != Material.AIR) {
                    eq.setHelmet(snap.helmet);
                } else {
                    eq.setHelmet(getOrCreatePlayerSkull(snap.uuid, snap.name));
                }
                eq.setChestplate(snap.chestplate);
                eq.setLeggings(snap.leggings);
                eq.setBoots(snap.boots);
                eq.setItemInMainHand(snap.mainHand);
                eq.setItemInOffHand(snap.offHand);
            }

            if (snap.hurt && snap.location.getWorld() != null) {
                for (Player viewer : Bukkit.getOnlinePlayers()) {
                    playSoundSafe(viewer, SOUND_PLAYER_HURT, 1.0f, 1.0f);
                }
                try {
                    snap.location.getWorld().playEffect(snap.location.clone().add(0, 1.0, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
                } catch (Throwable ignored) {
                }
            }
        }

        Iterator<Map.Entry<UUID, ArmorStand>> playerIt = replayPlayerClones.entrySet().iterator();
        while (playerIt.hasNext()) {
            Map.Entry<UUID, ArmorStand> entry = playerIt.next();
            if (!activePlayersInFrame.contains(entry.getKey())) {
                if (entry.getValue() != null) entry.getValue().remove();
                playerIt.remove();
            }
        }

        Set<UUID> activeEntitiesInFrame = new HashSet<>();
        for (EntitySnapshot es : frame.entities) {
            activeEntitiesInFrame.add(es.uuid);
            Entity clone = replayEntityClones.get(es.uuid);
            if (clone == null || !clone.isValid()) {
                World w = es.location.getWorld();
                if (w == null) continue;
                try {
                    if (es.droppedItem != null) {
                        Item dropped = w.dropItem(es.location, es.droppedItem);
                        dropped.setPickupDelay(Integer.MAX_VALUE);
                        dropped.setGravity(false);
                        dropped.setVelocity(new Vector(0, 0, 0));
                        clone = dropped;
                    } else if (es.type.isSpawnable()) {
                        clone = w.spawnEntity(es.location, es.type);
                        clone.setGravity(false);
                        clone.setInvulnerable(true);
                        clone.setVelocity(new Vector(0, 0, 0));
                        if (clone instanceof Mob) {
                            ((Mob) clone).setAI(false);
                        }
                        if (clone instanceof LivingEntity) {
                            EntityEquipment eq = ((LivingEntity) clone).getEquipment();
                            if (eq != null) {
                                eq.setHelmet(es.helmet);
                                eq.setChestplate(es.chestplate);
                                eq.setLeggings(es.leggings);
                                eq.setBoots(es.boots);
                                eq.setItemInMainHand(es.mainHand);
                            }
                        }
                    }
                    if (clone != null) {
                        replayEntityClones.put(es.uuid, clone);
                    }
                } catch (Throwable ignored) {
                }
            } else {
                clone.teleport(es.location);
                clone.setVelocity(new Vector(0, 0, 0));
            }
        }

        Iterator<Map.Entry<UUID, Entity>> entIt = replayEntityClones.entrySet().iterator();
        while (entIt.hasNext()) {
            Map.Entry<UUID, Entity> entry = entIt.next();
            if (!activeEntitiesInFrame.contains(entry.getKey())) {
                if (entry.getValue() != null) entry.getValue().remove();
                entIt.remove();
            }
        }
    }

    @SuppressWarnings("deprecation")
    private ItemStack getOrCreatePlayerSkull(UUID uuid, String playerName) {
        ItemStack cached = cachedPlayerSkulls.get(uuid);
        if (cached != null) return cached.clone();
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        try {
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
                skull.setItemMeta(meta);
            }
        } catch (Throwable ignored) {
        }
        cachedPlayerSkulls.put(uuid, skull);
        return skull.clone();
    }

    private void cleanupReplayEntities() {
        for (ArmorStand stand : replayPlayerClones.values()) {
            if (stand != null) stand.remove();
        }
        replayPlayerClones.clear();
        for (Entity ent : replayEntityClones.values()) {
            if (ent != null) ent.remove();
        }
        replayEntityClones.clear();
    }

    // =========================================================================
    // SCOREBOARD СПРАВА И ТАЙМЕР ИГРЫ (С УСЛОВИЕМ IMPOSSIBLE 4 ЧАСА)
    // =========================================================================

    private void startScoreboardAndTimerTask() {
        scoreboardAndTimerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (isPreparationStage() || gameState == GameState.COUNTDOWN) {
                    World overworld = getMainOverworld();
                    if (overworld != null) {
                        overworld.setTime(0L);
                    }
                }

                if (!gamePaused && !longTermStopped) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getWalkSpeed() < 0.05f) {
                            restorePlayerMovement(p);
                        }
                    }
                }

                if (gameState == GameState.IN_GAME && !gamePaused && !longTermStopped) {
                    elapsedGameSeconds++;

                    if (selectedDifficulty == DifficultyMode.IMPOSSIBLE
                            && elapsedGameSeconds >= IMPOSSIBLE_TIME_LIMIT_SECONDS) {
                        Player runner = speedrunnerUuid != null ? Bukkit.getPlayer(speedrunnerUuid) : null;
                        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "[Manhunt] Истекли 4 часа в режиме IMPOSSIBLE! Спидранер погибает!");
                        if (runner != null && runner.isOnline() && !runner.isDead()) {
                            runner.setHealth(0.0);
                        } else {
                            triggerHuntersWinFinal();
                        }
                    }
                }

                if (scoreboardActive) {
                    updateSidebarScoreboard();
                }
            }
        }.runTaskTimer(this, 20L, 20L);
    }

    private void updateSidebarScoreboard() {
        if (!scoreboardActive || Bukkit.getScoreboardManager() == null) return;
        try {
            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

            Objective oldObj = board.getObjective(OBJECTIVE_SIDEBAR);
            if (oldObj != null) {
                oldObj.unregister();
            }

            Objective obj = board.registerNewObjective(
                    OBJECTIVE_SIDEBAR,
                    "dummy",
                    ChatColor.GOLD + "" + ChatColor.BOLD + "✦ "
                            + ChatColor.RED + ChatColor.BOLD + "ᴀꜱᴏɴꜱꜱꜱ "
                            + ChatColor.BLUE + ChatColor.BOLD + "ᴍᴀɴʜᴜɴᴛ"
                            + ChatColor.GOLD + ChatColor.BOLD + " ✦"
            );
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);

            String diffText = selectedDifficulty != null
                    ? selectedDifficulty.getColoredDisplayName()
                    : ChatColor.GRAY + "Не выбран";

            String runnerName = "Не выбран";
            if (speedrunnerUuid != null) {
                Player sr = Bukkit.getPlayer(speedrunnerUuid);
                if (sr != null) {
                    runnerName = sr.getName();
                } else {
                    runnerName = Bukkit.getOfflinePlayer(speedrunnerUuid).getName();
                    if (runnerName == null) runnerName = "Выбран";
                }
            }

            String timerLabel;
            String timerValue;
            if (selectedDifficulty == DifficultyMode.IMPOSSIBLE) {
                long remaining = Math.max(0L, IMPOSSIBLE_TIME_LIMIT_SECONDS - elapsedGameSeconds);
                timerLabel = ChatColor.LIGHT_PURPLE + "Осталось времени до конца: ";
                timerValue = ChatColor.RED + formatHhMmSs(remaining);
            } else {
                timerLabel = ChatColor.YELLOW + "Время: ";
                timerValue = ChatColor.GREEN + formatHhMmSs(elapsedGameSeconds);
            }

            List<String> lines = Arrays.asList(
                    ChatColor.WHITE + "Сложность: " + diffText,
                    ChatColor.DARK_GRAY + "====================",
                    ChatColor.WHITE + "Спидранер: " + ChatColor.RED + runnerName,
                    ChatColor.WHITE + "Охотники: " + ChatColor.BLUE + hunterUuids.size(),
                    ChatColor.DARK_GRAY + "====================§r",
                    timerLabel + timerValue,
                    ChatColor.DARK_GRAY + "====================§7",
                    ChatColor.GOLD + "/manhunt info - информация о моде"
            );

            int score = lines.size();
            for (String line : lines) {
                obj.getScore(line).setScore(score--);
            }

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setScoreboard(board);
            }
        } catch (Throwable t) {
            getLogger().warning("Ошибка обновления Scoreboard: " + t.getMessage());
        }
    }

    private void clearSidebarScoreboard() {
        scoreboardActive = false;
        try {
            if (Bukkit.getScoreboardManager() == null) return;
            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
            Objective obj = board.getObjective(OBJECTIVE_SIDEBAR);
            if (obj != null) {
                obj.unregister();
            }
        } catch (Throwable ignored) {
        }
    }

    private String formatHhMmSs(long totalSeconds) {
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    // =========================================================================
    // ЗОНА ОЖИДАНИЯ (КОРОБКА 20x20x20 ИЗ БАРЬЕРОВ НА ВЫСОТЕ 250) И ПРАВИЛА МИРА
    // =========================================================================

    private Location buildWaitingBox(World world) {
        for (int cx = -1; cx <= 1; cx++) {
            for (int cz = -1; cz <= 1; cz++) {
                Chunk chunk = world.getChunkAt(cx, cz);
                if (!chunk.isLoaded()) {
                    chunk.load(true);
                }
            }
        }

        for (int x = BOX_MIN_X; x <= BOX_MAX_X; x++) {
            for (int y = BOX_MIN_Y; y <= BOX_MAX_Y; y++) {
                for (int z = BOX_MIN_Z; z <= BOX_MAX_Z; z++) {
                    boolean isWall = (x == BOX_MIN_X || x == BOX_MAX_X
                            || y == BOX_MIN_Y || y == BOX_MAX_Y
                            || z == BOX_MIN_Z || z == BOX_MAX_Z);
                    Block block = world.getBlockAt(x, y, z);
                    block.setType(isWall ? Material.BARRIER : Material.AIR, false);
                }
            }
        }
        waitingBoxBuilt = true;
        return new Location(world, 0.5, BOX_MIN_Y + 1.0, 0.5, 0.0f, 0.0f);
    }

    private void removeWaitingBox() {
        if (!waitingBoxBuilt) return;
        World world = getMainOverworld();
        if (world != null) {
            for (int x = BOX_MIN_X; x <= BOX_MAX_X; x++) {
                for (int y = BOX_MIN_Y; y <= BOX_MAX_Y; y++) {
                    for (int z = BOX_MIN_Z; z <= BOX_MAX_Z; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() == Material.BARRIER) {
                            block.setType(Material.AIR, false);
                        }
                    }
                }
            }
        }
        waitingBoxBuilt = false;
    }

    private int getHighestSurfaceYIgnoringBarriers(World world, int x, int z) {
        Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
        if (!chunk.isLoaded()) {
            chunk.load(true);
        }
        int maxY = world.getMaxHeight() - 1;
        int minY = world.getMinHeight();
        for (int y = maxY; y >= minY; y--) {
            Material type = world.getBlockAt(x, y, z).getType();
            if (type != Material.AIR && type != Material.CAVE_AIR && type != Material.VOID_AIR && type != Material.BARRIER) {
                return y;
            }
        }
        return 64;
    }

    @SuppressWarnings("deprecation")
    private void applyPreparationWorldRules(World overworld, Location boxSpawn) {
        for (World w : Bukkit.getWorlds()) {
            try {
                w.setPVP(false);
                w.setTime(0L);
                w.setGameRuleValue("doDaylightCycle", "false");
                w.setGameRuleValue("spawnRadius", "0");
            } catch (Throwable ignored) {
            }
        }
        dispatchConsoleCommandSafe("gamerule doDaylightCycle false");
        dispatchConsoleCommandSafe("gamerule spawnRadius 0");
        dispatchConsoleCommandSafe("time set 0");

        chosenStartLocation = boxSpawn.clone();
        try {
            overworld.setSpawnLocation(boxSpawn);
        } catch (Throwable ignored) {
            overworld.setSpawnLocation(boxSpawn.getBlockX(), boxSpawn.getBlockY(), boxSpawn.getBlockZ());
        }
        dispatchInWorldCommand(overworld, "setworldspawn 0 " + boxSpawn.getBlockY() + " 0");

        PotionEffectType saturationType = resolvePotionEffect("saturation", "SATURATION");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.SURVIVAL);
            p.setFallDistance(0f);
            p.teleport(boxSpawn);
            setPlayerRespawnPoint(p, boxSpawn);
            p.setFoodLevel(20);
            p.setSaturation(20f);
            if (saturationType != null) {
                p.addPotionEffect(new PotionEffect(saturationType, 20 * 60 * 60 * 24, 255, false, false, false));
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void applyActiveGameWorldRules() {
        PotionEffectType saturationType = resolvePotionEffect("saturation", "SATURATION");
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (saturationType != null) {
                p.removePotionEffect(saturationType);
            }
        }

        for (World w : Bukkit.getWorlds()) {
            try {
                w.setPVP(true);
                w.setGameRuleValue("doDaylightCycle", "true");
            } catch (Throwable ignored) {
            }
        }
        dispatchConsoleCommandSafe("gamerule doDaylightCycle true");
        disableLocatorBarEverywhere();
    }

    private void disableLocatorBarEverywhere() {
        setLocatorBarAllWorlds(false);
    }

    private void setLocatorBarAllWorlds(boolean enabled) {
        String valStr = String.valueOf(enabled);
        for (World w : Bukkit.getWorlds()) {
            setLocatorBarInWorld(w, enabled);
        }
        dispatchConsoleCommandSafe("gamerule locatorBar " + valStr);
        dispatchConsoleCommandSafe("gamerule locator_bar " + valStr);
        dispatchConsoleCommandSafe("execute in minecraft:overworld run gamerule locatorBar " + valStr);
        dispatchConsoleCommandSafe("execute in minecraft:overworld run gamerule locator_bar " + valStr);
        dispatchConsoleCommandSafe("execute in minecraft:the_nether run gamerule locatorBar " + valStr);
        dispatchConsoleCommandSafe("execute in minecraft:the_nether run gamerule locator_bar " + valStr);
        dispatchConsoleCommandSafe("execute in minecraft:the_end run gamerule locatorBar " + valStr);
        dispatchConsoleCommandSafe("execute in minecraft:the_end run gamerule locator_bar " + valStr);
    }

    @SuppressWarnings("deprecation")
    private void setLocatorBarInWorld(World w, boolean enabled) {
        if (w == null) return;
        String valStr = String.valueOf(enabled);
        try {
            w.setGameRuleValue("locatorBar", valStr);
        } catch (Throwable ignored) {
        }
        try {
            w.setGameRuleValue("locator_bar", valStr);
        } catch (Throwable ignored) {
        }
        try {
            String keyStr = w.getKey().toString();
            dispatchConsoleCommandSafe("execute in " + keyStr + " run gamerule locatorBar " + valStr);
            dispatchConsoleCommandSafe("execute in " + keyStr + " run gamerule locator_bar " + valStr);
        } catch (Throwable ignored) {
        }
    }

    private void dispatchWorldBorderCommand(World world, String subCommand) {
        dispatchInWorldCommand(world, "worldborder " + subCommand);
    }

    private void dispatchInWorldCommand(World world, String command) {
        if (world != null) {
            try {
                String keyStr = world.getKey().toString();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in " + keyStr + " run " + command);
                return;
            } catch (Throwable ignored) {
            }
        }
        dispatchConsoleCommandSafe(command);
    }

    @SuppressWarnings("deprecation")
    private void setPlayerRespawnPoint(Player player, Location location) {
        try {
            Method setRespawn = player.getClass().getMethod("setRespawnLocation", Location.class, boolean.class);
            setRespawn.invoke(player, location, true);
            return;
        } catch (Throwable ignored) {
        }
        try {
            player.setBedSpawnLocation(location, true);
        } catch (Throwable ignored) {
        }
    }

    private void dispatchConsoleCommandSafe(String commandLine) {
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandLine);
        } catch (Throwable ignored) {
        }
    }

    private void playSoundSafe(Player player, String soundKey, float volume, float pitch) {
        try {
            player.playSound(player.getLocation(), soundKey, volume, pitch);
        } catch (Throwable ignored) {
        }
    }

    // =========================================================================
    // ЭТАП 1: УПРАВЛЕНИЕ ПОДГОТОВКОЙ И РОЛЯМИ (С АВТОФИЛЬТРАЦИЕЙ МИРА)
    // =========================================================================

    private boolean isPreparationStage() {
        return gameState == GameState.PREPARE
                || gameState == GameState.DIFFICULTY_SELECTED
                || gameState == GameState.PICKING_PLACE
                || gameState == GameState.WAITING_READY;
    }

    private void startPreparePhase(CommandSender sender) {
        resetInternalState(false);
        gameState = GameState.PREPARE;
        elapsedGameSeconds = 0L;
        scoreboardActive = true;

        World defaultWorld = getMainOverworld();
        if (isWorldOceanFreeInRadius200(defaultWorld)) {
            activeOverworld = defaultWorld;
            sender.sendMessage(ChatColor.GREEN + "[Manhunt] Текущий мир прошёл проверку фильтра сидов (океана в радиусе 200 блоков от 0:0 нет).");
        } else {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "[Manhunt] На (0, 0) обнаружен океан в радиусе 200 блоков! Генерируем новый мир с подходящим сидом...");
            activeOverworld = createFilteredManhuntWorld(sender);
        }

        Location boxSpawn = buildWaitingBox(activeOverworld);
        applyPreparationWorldRules(activeOverworld, boxSpawn);

        updateSidebarScoreboard();

        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");
        Bukkit.broadcastMessage(ChatColor.GREEN + "Подготовка к Manhunt начата!");
        Bukkit.broadcastMessage(ChatColor.AQUA + "Все игроки перенесены в коробку ожидания (0, 250, 0).");
        Bukkit.broadcastMessage(ChatColor.GRAY + "Мир проверен (без океана у 0:0), ПВП и голод выключены.");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Администратор должен назначить спидранера и охотников:");
        Bukkit.broadcastMessage(ChatColor.GRAY + "• /manhunt speedruner add <Игрок>");
        Bukkit.broadcastMessage(ChatColor.GRAY + "• /manhunt hunter add <Игрок>");
        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");
    }

    private void addSpeedrunner(CommandSender sender, Player target) {
        if (speedrunnerUuid != null) {
            Player current = Bukkit.getPlayer(speedrunnerUuid);
            String currentName = current != null ? current.getName() : speedrunnerUuid.toString();
            sender.sendMessage(ChatColor.RED + "Спидранер уже выбран (" + currentName + ")! Сначала удалите его: /manhunt speedruner remove " + currentName);
            return;
        }

        if (hunterUuids.remove(target.getUniqueId())) {
            removeHunterCompass(target);
            removePlayerFromScoreboardTeam(target);
        }

        speedrunnerUuid = target.getUniqueId();
        assignPlayerToTeam(target, TEAM_SPEEDRUNNER, ChatColor.RED);
        updateSidebarScoreboard();

        target.sendTitle(ChatColor.RED + "Вы спидранер", ChatColor.YELLOW + "Выберите сложность: /sp difficulty select ...", 10, 70, 20);
        target.sendMessage(ChatColor.GREEN + "Вам разблокирован 1-й шаг подготовки: "
                + ChatColor.YELLOW + "/sp difficulty select <easy|medium|hard|impossible>");

        Bukkit.broadcastMessage(ChatColor.GOLD + "[Manhunt] " + ChatColor.RED + target.getName() + ChatColor.YELLOW + " назначен Спидранером!");
    }

    private void removeSpeedrunner(CommandSender sender, Player target) {
        if (speedrunnerUuid == null || !speedrunnerUuid.equals(target.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Игрок " + target.getName() + " не является спидранером.");
            return;
        }

        speedrunnerUuid = null;
        selectedDifficulty = null;
        if (gameState == GameState.DIFFICULTY_SELECTED || gameState == GameState.PICKING_PLACE) {
            gameState = GameState.PREPARE;
        }

        resetPlayerHealthAndMode(target);
        if (isPreparationStage()) {
            PotionEffectType sat = resolvePotionEffect("saturation", "SATURATION");
            if (sat != null) {
                target.addPotionEffect(new PotionEffect(sat, 20 * 60 * 60 * 24, 255, false, false, false));
            }
            if (chosenStartLocation != null) {
                target.teleport(chosenStartLocation);
            }
        }
        removePlayerFromScoreboardTeam(target);
        updateSidebarScoreboard();

        sender.sendMessage(ChatColor.GREEN + "Игрок " + target.getName() + " больше не спидранер.");
        target.sendMessage(ChatColor.YELLOW + "С вас снята роль спидранера.");
    }

    private void addHunter(CommandSender sender, Player target) {
        if (speedrunnerUuid != null && speedrunnerUuid.equals(target.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Этот игрок уже назначен спидранером! Сначала снимите с него роль спидранера.");
            return;
        }
        if (hunterUuids.contains(target.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Игрок " + target.getName() + " уже является охотником.");
            return;
        }

        hunterUuids.add(target.getUniqueId());
        assignPlayerToTeam(target, TEAM_HUNTERS, ChatColor.BLUE);
        giveHunterCompass(target);
        updateSidebarScoreboard();

        target.sendTitle(ChatColor.BLUE + "Вы охотник", ChatColor.AQUA + "Ваша задача — остановить спидранера!", 10, 70, 20);
        Bukkit.broadcastMessage(ChatColor.GOLD + "[Manhunt] " + ChatColor.BLUE + target.getName() + ChatColor.YELLOW + " назначен Охотником!");
    }

    private void removeHunter(CommandSender sender, Player target) {
        if (!hunterUuids.remove(target.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Игрок " + target.getName() + " не является охотником.");
            return;
        }

        removeHunterCompass(target);
        removePlayerFromScoreboardTeam(target);
        updateSidebarScoreboard();

        sender.sendMessage(ChatColor.GREEN + "Игрок " + target.getName() + " удалён из команды охотников.");
        target.sendMessage(ChatColor.YELLOW + "С вас снята роль охотника.");
    }

    private void resetGameFull(CommandSender sender) {
        resetInternalState(true);
        clearSidebarScoreboard();
        clearSavedGameStateFile();
        gameState = GameState.IDLE;
        Bukkit.broadcastMessage(ChatColor.GOLD + "[Manhunt] " + ChatColor.RED
                + "Весь процесс подготовки и настройки игры полностью сброшены администратором.");
    }

    @SuppressWarnings("deprecation")
    private void endGameCleanup(CommandSender sender) {
        cancelCountdown();
        if (replayPlaybackTask != null) {
            replayPlaybackTask.cancel();
            replayPlaybackTask = null;
        }
        cleanupReplayEntities();
        longTermStopped = false;
        if (stopBossBar != null) {
            stopBossBar.removeAll();
            stopBossBar = null;
        }
        stopPauseImmediately(true);
        removeWaitingBox();
        clearSidebarScoreboard();
        clearSavedGameStateFile();

        World overworld = getMainOverworld();
        int surfaceY = getHighestSurfaceYIgnoringBarriers(overworld, 0, 0) + 1;
        Location defaultSpawn = new Location(overworld, 0.5, surfaceY, 0.5);
        try {
            overworld.setSpawnLocation(defaultSpawn);
        } catch (Throwable ignored) {
            overworld.setSpawnLocation(0, surfaceY, 0);
        }
        dispatchInWorldCommand(overworld, "setworldspawn 0 " + surfaceY + " 0");

        resetWorldBorder(overworld);
        for (World w : Bukkit.getWorlds()) {
            try {
                w.setPVP(true);
                w.setGameRuleValue("doDaylightCycle", "true");
            } catch (Throwable ignored) {
            }
        }
        dispatchConsoleCommandSafe("gamerule doDaylightCycle true");
        setLocatorBarAllWorlds(true);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGlowing(false);
            restorePlayerMovement(p);
            removePlayerFromScoreboardTeam(p);
            removeHunterCompass(p);
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            setPlayerMaxHealth(p, 20.0);
            p.setGameMode(GameMode.SURVIVAL);
            setPlayerRespawnPoint(p, defaultSpawn);
            p.setHealth(0.0);
            p.teleport(defaultSpawn);
        }

        speedrunnerUuid = null;
        hunterUuids.clear();
        readyPlayers.clear();
        selectedDifficulty = null;
        chosenStartLocation = null;
        lastRunnerLocations.clear();
        trackedRunnerEffects.clear();
        runnerOverworldPortalBlocks.clear();
        runnerInitialNetherPortalBlocks.clear();
        runnerInitialNetherPortalCenter = null;
        replayFrames.clear();
        frozenLocationsDuringStop.clear();
        elapsedGameSeconds = 0L;
        pauseCooldownEndMillis = 0L;
        combatCooldownEndMillis = 0L;
        gameState = GameState.IDLE;

        Bukkit.broadcastMessage(ChatColor.GOLD + "[Manhunt] " + ChatColor.GREEN
                + "Игра завершена администратором. Все роли и команды сброшены.");
    }

    // =========================================================================
    // ЭТАП 2: ВЫБОР МЕСТА СТАРТА И ГОТОВНОСТЬ
    // =========================================================================

    private void startPickPlace(Player speedrunner) {
        gameState = GameState.PICKING_PLACE;

        World overworld = getMainOverworld();
        WorldBorder border = overworld.getWorldBorder();
        border.setCenter(0.0, 0.0);
        border.setSize(selectedDifficulty.getPickPlaceBorderSize());
        dispatchWorldBorderCommand(overworld, "center 0 0");
        dispatchWorldBorderCommand(overworld, "set " + (int) selectedDifficulty.getPickPlaceBorderSize());

        int highestY = getHighestSurfaceYIgnoringBarriers(overworld, 0, 0);
        Location centerLoc = new Location(overworld, 0.5, highestY + 10.0, 0.5);

        speedrunner.setGameMode(GameMode.SPECTATOR);
        speedrunner.setAllowFlight(true);
        speedrunner.setFlying(true);
        speedrunner.teleport(centerLoc);

        int zoneSize = (int) selectedDifficulty.getPickPlaceBorderSize();
        speedrunner.sendMessage(ChatColor.GREEN + "Вы переведены в режим Наблюдателя в зоне "
                + zoneSize + "x" + zoneSize + " блоков от (0, 0).");
        speedrunner.sendMessage(ChatColor.AQUA + "Если мир не понравился, введите: " + ChatColor.GOLD + "/sp restart");
        speedrunner.sendMessage(ChatColor.YELLOW + "Когда выберете место старта, введите: " + ChatColor.GOLD + "/sp pickplace end");
    }

    private void endPickPlace(Player speedrunner) {
        Location runnerLoc = speedrunner.getLocation();
        World world = runnerLoc.getWorld() != null ? runnerLoc.getWorld() : getMainOverworld();

        removeWaitingBox();

        double halfZone = selectedDifficulty != null ? (selectedDifficulty.getPickPlaceBorderSize() / 2.0 - 0.5) : 249.5;
        double exactX = Math.max(-halfZone, Math.min(halfZone, runnerLoc.getX()));
        double exactZ = Math.max(-halfZone, Math.min(halfZone, runnerLoc.getZ()));
        int blockX = Location.locToBlock(exactX);
        int blockZ = Location.locToBlock(exactZ);
        int topY = getHighestSurfaceYIgnoringBarriers(world, blockX, blockZ) + 1;

        chosenStartLocation = new Location(world, exactX, topY, exactZ, runnerLoc.getYaw(), runnerLoc.getPitch());

        try {
            world.setSpawnLocation(chosenStartLocation);
        } catch (Throwable ignored) {
            world.setSpawnLocation(blockX, topY, blockZ);
        }
        dispatchInWorldCommand(world, "setworldspawn " + blockX + " " + topY + " " + blockZ);

        PotionEffectType saturationType = resolvePotionEffect("saturation", "SATURATION");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setFallDistance(0f);
            p.teleport(chosenStartLocation);
            setPlayerRespawnPoint(p, chosenStartLocation);
            if (!p.getUniqueId().equals(speedrunnerUuid)) {
                p.setGameMode(GameMode.SURVIVAL);
                setPlayerMaxHealth(p, 20.0);
                p.setHealth(20.0);
                p.setFoodLevel(20);
            }
            if (saturationType != null) {
                p.addPotionEffect(new PotionEffect(saturationType, 20 * 60 * 60 * 24, 255, false, false, false));
            }
        }

        speedrunner.setGameMode(GameMode.SURVIVAL);
        double maxHp = selectedDifficulty.getSpeedrunnerMaxHealth();
        setPlayerMaxHealth(speedrunner, maxHp);
        speedrunner.setHealth(maxHp);
        speedrunner.setFoodLevel(20);
        speedrunner.setSaturation(20f);

        WorldBorder border = world.getWorldBorder();
        border.setCenter(exactX, exactZ);
        border.setSize(PRE_GAME_BORDER_SIZE);
        dispatchWorldBorderCommand(world, "center " + exactX + " " + exactZ);
        dispatchWorldBorderCommand(world, "set 30");

        readyPlayers.clear();
        gameState = GameState.WAITING_READY;

        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");
        Bukkit.broadcastMessage(ChatColor.GREEN + "Спидранер выбрал стартовую локацию!");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Всем игрокам доступны команды готовности:");
        Bukkit.broadcastMessage(ChatColor.GREEN + "• /ready " + ChatColor.GRAY + "— подтвердить готовность (включает ауру)");
        Bukkit.broadcastMessage(ChatColor.RED + "• /unready " + ChatColor.GRAY + "— отменить готовность");
        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");
    }

    private int getRequiredPlayersCount() {
        Set<UUID> participants = new HashSet<>(hunterUuids);
        if (speedrunnerUuid != null) {
            participants.add(speedrunnerUuid);
        }
        int onlineParticipants = 0;
        for (UUID uuid : participants) {
            if (Bukkit.getPlayer(uuid) != null) {
                onlineParticipants++;
            }
        }
        return onlineParticipants > 0 ? onlineParticipants : Bukkit.getOnlinePlayers().size();
    }

    private void checkAllPlayersReady() {
        Set<UUID> required = new HashSet<>(hunterUuids);
        if (speedrunnerUuid != null) {
            required.add(speedrunnerUuid);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (required.isEmpty() || required.contains(p.getUniqueId())) {
                if (!readyPlayers.contains(p.getUniqueId())) {
                    return;
                }
            }
        }

        if (gameState == GameState.WAITING_READY) {
            startCountdown();
        }
    }

    private void startCountdown() {
        cancelCountdown();
        gameState = GameState.COUNTDOWN;

        World world = getGameBorderWorld();
        dispatchWorldBorderCommand(world, "set 30");
        dispatchWorldBorderCommand(world, "set 5 15s");

        countdownTask = new BukkitRunnable() {
            int secondsLeft = 15;

            @Override
            public void run() {
                if (secondsLeft > 0) {
                    ChatColor color = secondsLeft <= 3 ? ChatColor.RED
                            : (secondsLeft <= 5 ? ChatColor.GOLD : ChatColor.GREEN);
                    float notePitch = getCountdownPianoPitch(secondsLeft);

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(color + String.valueOf(secondsLeft),
                                ChatColor.YELLOW + "Приготовьтесь к старту!", 0, 25, 5);
                        playSoundSafe(p, SOUND_ELECTRIC_PIANO, 1.0f, notePitch);
                    }
                    secondsLeft--;
                } else {
                    this.cancel();
                    countdownTask = null;
                    beginActiveGame();
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private float getCountdownPianoPitch(int secondsLeft) {
        if (secondsLeft >= 5) {
            return PITCH_C_OCTAVE_1;
        }
        switch (secondsLeft) {
            case 4:
                return PITCH_E_OCTAVE_1;
            case 3:
                return PITCH_G_OCTAVE_2;
            case 2:
                return PITCH_C_OCTAVE_2;
            case 1:
                return PITCH_E_OCTAVE_2;
            default:
                return PITCH_C_OCTAVE_1;
        }
    }

    private void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }

    private void cancelCountdownAndRestoreBorder() {
        cancelCountdown();
        gameState = GameState.WAITING_READY;
        dispatchWorldBorderCommand(getGameBorderWorld(), "set 30 1s");
    }

    private World getGameBorderWorld() {
        return chosenStartLocation != null && chosenStartLocation.getWorld() != null
                ? chosenStartLocation.getWorld()
                : getMainOverworld();
    }

    // =========================================================================
    // ЭТАП 3: НАЧАЛО И ХОД ИГРЫ
    // =========================================================================

    private void beginActiveGame() {
        gameState = GameState.IN_GAME;
        elapsedGameSeconds = 0L;
        pauseCooldownEndMillis = 0L;
        combatCooldownEndMillis = 0L;
        runnerOverworldPortalBlocks.clear();
        runnerInitialNetherPortalBlocks.clear();
        runnerInitialNetherPortalCenter = null;
        replayFrames.clear();
        pendingBlockChangesThisTick.clear();

        removeWaitingBox();
        applyActiveGameWorldRules();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGlowing(false);
            restorePlayerMovement(p);
            p.sendTitle(ChatColor.GREEN + "ИГРА НАЧАЛАСЬ!", ChatColor.YELLOW + "Удачи!", 5, 40, 10);
            playSoundSafe(p, SOUND_LEVELUP, 1.0f, 1.0f);
            playSoundSafe(p, SOUND_WITHER_SPAWN, 1.0f, 1.0f);
        }
        readyPlayers.clear();

        World world = getGameBorderWorld();
        WorldBorder border = world.getWorldBorder();
        if (chosenStartLocation != null) {
            border.setCenter(chosenStartLocation.getX(), chosenStartLocation.getZ());
            dispatchWorldBorderCommand(world, "center " + chosenStartLocation.getX() + " " + chosenStartLocation.getZ());
        }

        if (selectedDifficulty != null) {
            int targetSize = (int) selectedDifficulty.getGameBorderSize();
            dispatchWorldBorderCommand(world, "set " + targetSize + " 200s");

            if (selectedDifficulty == DifficultyMode.IMPOSSIBLE) {
                Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "[Manhunt] Режим IMPOSSIBLE: зона расширяется до 30 000 000 блоков за 200 сек.! У спидранера есть 4 часа!");
            } else {
                Bukkit.broadcastMessage(ChatColor.GOLD + "[Manhunt] " + ChatColor.YELLOW
                        + "Граница мира расширяется до " + ChatColor.AQUA + targetSize
                        + " блоков " + ChatColor.YELLOW + "за 200 секунд!");
            }
        }

        Player runner = speedrunnerUuid != null ? Bukkit.getPlayer(speedrunnerUuid) : null;
        if (runner != null && selectedDifficulty != null) {
            double maxHp = selectedDifficulty.getSpeedrunnerMaxHealth();
            setPlayerMaxHealth(runner, maxHp);
            runner.setHealth(maxHp);
            applyStartingEffects(runner, selectedDifficulty);
            lastRunnerLocations.put(runner.getWorld().getEnvironment(), runner.getLocation().clone());
        }

        for (UUID hunterId : hunterUuids) {
            Player hunter = Bukkit.getPlayer(hunterId);
            if (hunter != null) {
                giveHunterCompass(hunter);
            }
        }

        updateSidebarScoreboard();
    }

    private void applyStartingEffects(Player runner, DifficultyMode difficulty) {
        trackedRunnerEffects.clear();
        int hunterCount = Math.max(1, hunterUuids.size());

        PotionEffectType speedType = resolvePotionEffect("speed", "SPEED");
        PotionEffectType strengthType = resolvePotionEffect("strength", "STRENGTH", "INCREASE_DAMAGE");
        PotionEffectType regenType = resolvePotionEffect("regeneration", "REGENERATION");

        List<PotionEffect> effectsToAdd = new ArrayList<>();

        switch (difficulty) {
            case EASY:
                if (speedType != null) {
                    effectsToAdd.add(new PotionEffect(speedType, 20 * 60 * hunterCount, 4, false, true, true));
                }
                if (strengthType != null) {
                    effectsToAdd.add(new PotionEffect(strengthType, 20 * 600 * hunterCount, 1, false, true, true));
                }
                if (regenType != null) {
                    effectsToAdd.add(new PotionEffect(regenType, 20 * 900 * hunterCount, 1, false, true, true));
                }
                break;

            case MEDIUM:
                if (speedType != null) {
                    effectsToAdd.add(new PotionEffect(speedType, 20 * 30 * hunterCount, 4, false, true, true));
                }
                if (strengthType != null) {
                    effectsToAdd.add(new PotionEffect(strengthType, 20 * 300 * hunterCount, 0, false, true, true));
                }
                if (regenType != null) {
                    effectsToAdd.add(new PotionEffect(regenType, 20 * 300 * hunterCount, 1, false, true, true));
                }
                break;

            case HARD:
                if (speedType != null) {
                    effectsToAdd.add(new PotionEffect(speedType, 20 * 15 * hunterCount, 4, false, true, true));
                }
                break;

            case IMPOSSIBLE:
                break;
        }

        for (PotionEffect effect : effectsToAdd) {
            runner.addPotionEffect(effect);
            trackedRunnerEffects.add(effect.getType());
        }
    }

    @SuppressWarnings("deprecation")
    private PotionEffectType resolvePotionEffect(String... names) {
        for (String name : names) {
            try {
                PotionEffectType byKey = Registry.EFFECT.get(NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT)));
                if (byKey != null) return byKey;
            } catch (Throwable ignored) {
            }
            try {
                PotionEffectType byKey = PotionEffectType.getByKey(NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT)));
                if (byKey != null) return byKey;
            } catch (Throwable ignored) {
            }
            try {
                PotionEffectType byName = PotionEffectType.getByName(name);
                if (byName != null) return byName;
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private void startCompassAndEffectTask() {
        gameLoopTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (gameState != GameState.IN_GAME && gameState != GameState.WAITING_READY && gameState != GameState.COUNTDOWN) {
                    return;
                }
                if (gamePaused || longTermStopped) {
                    return;
                }

                Player runner = speedrunnerUuid != null ? Bukkit.getPlayer(speedrunnerUuid) : null;
                if (runner != null && runner.isOnline() && !runner.isDead()) {
                    lastRunnerLocations.put(runner.getWorld().getEnvironment(), runner.getLocation().clone());

                    if (gameState == GameState.IN_GAME && !trackedRunnerEffects.isEmpty()) {
                        Iterator<PotionEffectType> it = trackedRunnerEffects.iterator();
                        while (it.hasNext()) {
                            PotionEffectType type = it.next();
                            if (!runner.hasPotionEffect(type)) {
                                it.remove();
                                announceEffectExpired(type);
                            }
                        }
                    }
                }

                updateAllHunterCompasses(runner);
            }
        }.runTaskTimer(this, 10L, 10L);
    }

    private void updateAllHunterCompasses(Player runner) {
        for (UUID hunterId : hunterUuids) {
            Player hunter = Bukkit.getPlayer(hunterId);
            if (hunter == null || !hunter.isOnline()) continue;

            Location targetLoc = resolveCompassTargetForHunter(hunter, runner);
            if (targetLoc == null) continue;

            hunter.setCompassTarget(targetLoc);

            for (ItemStack item : hunter.getInventory().getContents()) {
                if (isHunterCompass(item)) {
                    CompassMeta meta = (CompassMeta) item.getItemMeta();
                    if (meta != null) {
                        meta.setLodestone(targetLoc);
                        meta.setLodestoneTracked(false);
                        item.setItemMeta(meta);
                    }
                }
            }
        }
    }

    private Location resolveCompassTargetForHunter(Player hunter, Player runner) {
        World hunterWorld = hunter.getWorld();
        if (runner != null && runner.isOnline() && runner.getWorld().equals(hunterWorld)) {
            return runner.getLocation();
        }
        Location lastInDimension = lastRunnerLocations.get(hunterWorld.getEnvironment());
        if (lastInDimension != null && lastInDimension.getWorld() != null && lastInDimension.getWorld().equals(hunterWorld)) {
            return lastInDimension;
        }
        return chosenStartLocation != null ? chosenStartLocation : hunterWorld.getSpawnLocation();
    }

    private void announceEffectExpired(PotionEffectType type) {
        String effectName = formatEffectName(type);
        Bukkit.broadcastMessage(ChatColor.GOLD + "[Manhunt] " + ChatColor.YELLOW
                + "У спидранера закончился эффект: " + ChatColor.RED + effectName + ChatColor.YELLOW + "!");
    }

    @SuppressWarnings("deprecation")
    private String formatEffectName(PotionEffectType type) {
        String name = "";
        try {
            name = type.getKey().getKey().toUpperCase(Locale.ROOT);
        } catch (Throwable ignored) {
            try {
                name = type.getName().toUpperCase(Locale.ROOT);
            } catch (Throwable ignored2) {
            }
        }
        if (name.contains("SPEED")) return "Скорость";
        if (name.contains("DAMAGE") || name.contains("STRENGTH")) return "Сила";
        if (name.contains("REGENERATION")) return "Регенерация";
        return name.isEmpty() ? "Эффект" : name;
    }

    private void sendWrongPortalWarning(Player player) {
        long now = System.currentTimeMillis();
        if (now - lastWrongPortalWarningMillis > 1200L) {
            lastWrongPortalWarningMillis = now;
            player.sendMessage(ChatColor.RED + "Для возращения войдите в свой первоначальный портал");
            playSoundSafe(player, SOUND_ELECTRIC_PIANO, 1.0f, 1.0f);
        }
    }

    private void collectPortalBlocksAround(Location center, int radius, Set<BlockPos> destinationSet) {
        if (center == null || center.getWorld() == null) return;
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        List<Block> portalBlocks = new ArrayList<>();
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = Math.max(world.getMinHeight(), cy - radius); y <= Math.min(world.getMaxHeight() - 1, cy + radius); y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType() == Material.NETHER_PORTAL) {
                        portalBlocks.add(b);
                        destinationSet.add(BlockPos.of(b));
                    }
                }
            }
        }

        for (Block pb : portalBlocks) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Block neighbor = pb.getRelative(dx, dy, dz);
                        if (neighbor.getType() == Material.OBSIDIAN || neighbor.getType() == Material.NETHER_PORTAL) {
                            destinationSet.add(BlockPos.of(neighbor));
                        }
                    }
                }
            }
        }
    }

    private boolean isLocationInInitialNetherPortal(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        if (runnerInitialNetherPortalBlocks.isEmpty() && runnerInitialNetherPortalCenter == null) {
            return true;
        }
        Block block = loc.getBlock();
        if (runnerInitialNetherPortalBlocks.contains(BlockPos.of(block))) {
            return true;
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (runnerInitialNetherPortalBlocks.contains(BlockPos.of(block.getRelative(dx, dy, dz)))) {
                        return true;
                    }
                }
            }
        }
        if (runnerInitialNetherPortalCenter != null
                && runnerInitialNetherPortalCenter.getWorld() != null
                && runnerInitialNetherPortalCenter.getWorld().equals(loc.getWorld())) {
            return runnerInitialNetherPortalCenter.distanceSquared(loc) <= 25.0;
        }
        return false;
    }

    private boolean isLocationTouchingNetherPortal(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        double[] offsets = {-0.35, 0.0, 0.35};
        for (double ox : offsets) {
            for (double oz : offsets) {
                for (double oy = 0.0; oy <= 1.5; oy += 0.75) {
                    Block b = loc.clone().add(ox, oy, oz).getBlock();
                    if (b.getType() == Material.NETHER_PORTAL) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isSpeedrunnerPortalBlockOrFrame(Block block) {
        if (block == null) return false;
        BlockPos pos = BlockPos.of(block);
        if (runnerOverworldPortalBlocks.contains(pos) || runnerInitialNetherPortalBlocks.contains(pos)) {
            return true;
        }
        if (block.getType() == Material.OBSIDIAN || block.getType() == Material.NETHER_PORTAL) {
            BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
            for (BlockFace face : faces) {
                BlockPos adj = BlockPos.of(block.getRelative(face));
                if (runnerOverworldPortalBlocks.contains(adj) || runnerInitialNetherPortalBlocks.contains(adj)) {
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPortalCreate(PortalCreateEvent event) {
        if (gameState != GameState.IN_GAME) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;
        Player player = (Player) entity;
        if (speedrunnerUuid == null || !speedrunnerUuid.equals(player.getUniqueId())) return;

        World world = event.getWorld();
        Set<BlockPos> targetSet = (world.getEnvironment() == World.Environment.NORMAL)
                ? runnerOverworldPortalBlocks
                : (world.getEnvironment() == World.Environment.NETHER && runnerInitialNetherPortalBlocks.isEmpty()
                ? runnerInitialNetherPortalBlocks
                : null);

        if (targetSet != null) {
            for (BlockState state : event.getBlocks()) {
                Block b = state.getBlock();
                targetSet.add(BlockPos.of(b));
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            Block neighbor = b.getRelative(dx, dy, dz);
                            if (neighbor.getType() == Material.OBSIDIAN || neighbor.getType() == Material.NETHER_PORTAL) {
                                targetSet.add(BlockPos.of(neighbor));
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // СОБЫТИЯ ИГРЫ, СХВАТКИ, ПОРТАЛОВ, ПОВТОРА И ЗАМОРОЗКИ (LISTENERS)
    // =========================================================================

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerArmSwing(PlayerAnimationEvent event) {
        if (gameState == GameState.IN_GAME && !gamePaused && !longTermStopped) {
            swungHandThisTick.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        if (gameState == GameState.IN_GAME) {
            setLocatorBarInWorld(event.getWorld(), false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        if (gamePaused || longTermStopped) {
            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                event.setTo(from);
            }
            return;
        }

        if (gameState == GameState.PICKING_PLACE
                && speedrunnerUuid != null
                && speedrunnerUuid.equals(player.getUniqueId())
                && selectedDifficulty != null) {
            double halfLimit = selectedDifficulty.getPickPlaceBorderSize() / 2.0 - 0.5;
            if (Math.abs(to.getX()) > halfLimit || Math.abs(to.getZ()) > halfLimit) {
                double clampedX = Math.max(-halfLimit, Math.min(halfLimit, to.getX()));
                double clampedZ = Math.max(-halfLimit, Math.min(halfLimit, to.getZ()));
                Location clamped = new Location(to.getWorld(), clampedX, to.getY(), clampedZ, to.getYaw(), to.getPitch());
                event.setTo(clamped);

                long now = System.currentTimeMillis();
                if (now - lastBorderWarningMillis > 1500L) {
                    lastBorderWarningMillis = now;
                    player.sendMessage(ChatColor.RED + "Вы не можете вылетать за пределы зоны выбора места для спавна!");
                }
                return;
            }
        }

        if (gameState != GameState.IN_GAME) return;

        if (speedrunnerUuid != null && speedrunnerUuid.equals(player.getUniqueId())) {
            World world = to.getWorld();
            if (world != null) {
                if (world.getEnvironment() == World.Environment.NETHER
                        && !runnerInitialNetherPortalBlocks.isEmpty()
                        && isLocationTouchingNetherPortal(to)
                        && !isLocationInInitialNetherPortal(to)) {
                    event.setTo(from);
                    Vector pushBack = from.toVector().subtract(to.toVector());
                    if (pushBack.lengthSquared() > 0.0001) {
                        pushBack.normalize().multiply(0.35);
                        player.setVelocity(pushBack);
                    }
                    sendWrongPortalWarning(player);
                    return;
                }

                lastRunnerLocations.put(world.getEnvironment(), to.clone());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        World fromWorld = from.getWorld();
        if (fromWorld == null) return;

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            if (fromWorld.getEnvironment() == World.Environment.NORMAL) {
                World nether = getNetherWorld();
                if (nether != null && (event.getTo() == null || event.getTo().getWorld() == null)) {
                    Location target = new Location(nether, from.getX() / 8.0, Math.max(32.0, Math.min(118.0, from.getY())), from.getZ() / 8.0);
                    event.setTo(target);
                    event.setCanCreatePortal(true);
                }
            } else if (fromWorld.getEnvironment() == World.Environment.NETHER) {
                World overworld = getMainOverworld();
                if (overworld != null) {
                    Location savedOwPortal = lastRunnerLocations.get(World.Environment.NORMAL);
                    if (savedOwPortal != null && savedOwPortal.getWorld() != null && savedOwPortal.getWorld().equals(overworld)) {
                        event.setTo(savedOwPortal);
                    } else if (event.getTo() == null || !overworld.equals(event.getTo().getWorld())) {
                        Location target = new Location(overworld, from.getX() * 8.0, from.getY(), from.getZ() * 8.0);
                        event.setTo(target);
                        event.setCanCreatePortal(true);
                    }
                }
            }
        } else if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            if (fromWorld.getEnvironment() == World.Environment.NORMAL) {
                World endWorld = getEndWorld();
                if (endWorld != null && (event.getTo() == null || event.getTo().getWorld() == null)) {
                    event.setTo(endWorld.getSpawnLocation());
                }
            }
        }

        if (speedrunnerUuid == null || !speedrunnerUuid.equals(player.getUniqueId())) return;

        if (gameState == GameState.IN_GAME) {
            if (fromWorld.getEnvironment() == World.Environment.NORMAL) {
                collectPortalBlocksAround(from, 5, runnerOverworldPortalBlocks);

                Location to = event.getTo();
                if (to != null && to.getWorld() != null && to.getWorld().getEnvironment() == World.Environment.NETHER) {
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        Player runner = Bukkit.getPlayer(speedrunnerUuid);
                        if (runner != null && runner.getWorld().getEnvironment() == World.Environment.NETHER) {
                            if (runnerInitialNetherPortalBlocks.isEmpty()) {
                                runnerInitialNetherPortalCenter = runner.getLocation().clone();
                                collectPortalBlocksAround(runner.getLocation(), 6, runnerInitialNetherPortalBlocks);
                            }
                        }
                    }, 2L);
                }
            }

            if (fromWorld.getEnvironment() == World.Environment.NETHER) {
                if (!runnerInitialNetherPortalBlocks.isEmpty() && !isLocationInInitialNetherPortal(from)) {
                    event.setCancelled(true);
                    sendWrongPortalWarning(player);
                    return;
                }
            }
        }

        lastRunnerLocations.put(fromWorld.getEnvironment(), from.clone());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World currentWorld = player.getWorld();

        if (gameState == GameState.IN_GAME) {
            setLocatorBarInWorld(currentWorld, false);
        }

        if (speedrunnerUuid != null && speedrunnerUuid.equals(player.getUniqueId())) {
            Location current = player.getLocation();
            if (current.getWorld() != null) {
                lastRunnerLocations.put(current.getWorld().getEnvironment(), current.clone());

                if (gameState == GameState.IN_GAME
                        && current.getWorld().getEnvironment() == World.Environment.NETHER
                        && runnerInitialNetherPortalBlocks.isEmpty()) {
                    runnerInitialNetherPortalCenter = current.clone();
                    collectPortalBlocksAround(current, 6, runnerInitialNetherPortalBlocks);
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        Player runner = Bukkit.getPlayer(speedrunnerUuid);
                        if (runner != null && runner.getWorld().getEnvironment() == World.Environment.NETHER) {
                            collectPortalBlocksAround(runner.getLocation(), 6, runnerInitialNetherPortalBlocks);
                        }
                    }, 3L);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (gamePaused || longTermStopped || gameState == GameState.REPLAYING_DEATH) {
            event.setCancelled(true);
            return;
        }

        if (gameState == GameState.IN_GAME) {
            Block block = event.getBlock();
            recordBlockChange(block, block.getBlockData().clone(), Material.AIR.createBlockData());

            Player breaker = event.getPlayer();
            if (hunterUuids.contains(breaker.getUniqueId())) {
                if (isSpeedrunnerPortalBlockOrFrame(block)) {
                    triggerSpeedrunnerWinByPortalBreak(breaker.getName());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (gamePaused || longTermStopped || gameState == GameState.REPLAYING_DEATH) {
            event.setCancelled(true);
            return;
        }
        if (gameState == GameState.IN_GAME) {
            Block block = event.getBlockPlaced();
            recordBlockChange(block, event.getBlockReplacedState().getBlockData().clone(), block.getBlockData().clone());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (gameState == GameState.IN_GAME && !gamePaused && !longTermStopped) {
            for (Block b : event.blockList()) {
                recordBlockChange(b, b.getBlockData().clone(), Material.AIR.createBlockData());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (gameState == GameState.IN_GAME && !gamePaused && !longTermStopped) {
            for (Block b : event.blockList()) {
                recordBlockChange(b, b.getBlockData().clone(), Material.AIR.createBlockData());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (gamePaused || longTermStopped || gameState == GameState.REPLAYING_DEATH) {
            event.setCancelled(true);
            return;
        }

        if (gameState == GameState.IN_GAME) {
            Block targetBlock = event.getBlock();
            Material liquid = event.getBucket() == Material.LAVA_BUCKET ? Material.LAVA : Material.WATER;
            recordBlockChange(targetBlock, targetBlock.getBlockData().clone(), liquid.createBlockData());

            Player player = event.getPlayer();
            if (hunterUuids.contains(player.getUniqueId())) {
                Block clickedBlock = event.getBlockClicked();
                if (isSpeedrunnerPortalBlockOrFrame(targetBlock) || isSpeedrunnerPortalBlockOrFrame(clickedBlock)) {
                    triggerSpeedrunnerWinByPortalBreak(player.getName());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (gamePaused || longTermStopped || gameState == GameState.REPLAYING_DEATH) {
            event.setCancelled(true);
            return;
        }

        if (isPreparationStage() || gameState == GameState.COUNTDOWN) {
            if (event.getEntity() instanceof Player) {
                Player attacker = resolveAttackerPlayer(event.getDamager());
                if (attacker != null) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (gameState == GameState.IN_GAME && event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = resolveAttackerPlayer(event.getDamager());
            if (attacker != null && !attacker.getUniqueId().equals(victim.getUniqueId())) {
                boolean attackerIsRunner = speedrunnerUuid != null && speedrunnerUuid.equals(attacker.getUniqueId());
                boolean victimIsRunner = speedrunnerUuid != null && speedrunnerUuid.equals(victim.getUniqueId());
                boolean attackerIsHunter = hunterUuids.contains(attacker.getUniqueId());
                boolean victimIsHunter = hunterUuids.contains(victim.getUniqueId());

                if ((attackerIsRunner && victimIsHunter) || (attackerIsHunter && victimIsRunner)) {
                    combatCooldownEndMillis = System.currentTimeMillis() + COMBAT_PAUSE_LOCK_MILLIS;
                }
            }
        }
    }

    private Player resolveAttackerPlayer(Entity damager) {
        if (damager instanceof Player) {
            return (Player) damager;
        }
        if (damager instanceof Projectile) {
            ProjectileSource source = ((Projectile) damager).getShooter();
            if (source instanceof Player) {
                return (Player) source;
            }
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (gamePaused || longTermStopped || gameState == GameState.REPLAYING_DEATH) {
            event.setCancelled(true);
            return;
        }
        if (gameState == GameState.IN_GAME && event.getEntity() instanceof Player) {
            hurtThisTick.add(event.getEntity().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (gamePaused || longTermStopped || gameState == GameState.REPLAYING_DEATH) {
            event.setCancelled(true);
            return;
        }
        if (gameState == GameState.IN_GAME && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            swungHandThisTick.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (gamePaused || longTermStopped || isPreparationStage() || gameState == GameState.COUNTDOWN) {
            event.setCancelled(true);
            if (event.getEntity() instanceof Player) {
                ((Player) event.getEntity()).setFoodLevel(20);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        if (speedrunnerUuid != null && speedrunnerUuid.equals(p.getUniqueId())) {
            assignPlayerToTeam(p, TEAM_SPEEDRUNNER, ChatColor.RED);
            if (selectedDifficulty != null && (gameState == GameState.IN_GAME || gameState == GameState.WAITING_READY)) {
                setPlayerMaxHealth(p, selectedDifficulty.getSpeedrunnerMaxHealth());
            }
        } else if (hunterUuids.contains(p.getUniqueId())) {
            assignPlayerToTeam(p, TEAM_HUNTERS, ChatColor.BLUE);
            giveHunterCompass(p);
        }

        if (scoreboardActive && Bukkit.getScoreboardManager() != null) {
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        if (pauseBossBar != null) {
            pauseBossBar.addPlayer(p);
        }
        if (stopBossBar != null) {
            stopBossBar.addPlayer(p);
        }

        if (longTermStopped) {
            Location savedLoc = frozenLocationsDuringStop.get(p.getUniqueId());
            if (savedLoc != null) {
                p.teleport(savedLoc);
            }
            p.setWalkSpeed(0.0f);
            p.setFlySpeed(0.0f);
            return;
        }

        if (!gamePaused) {
            restorePlayerMovement(p);
        } else {
            p.setWalkSpeed(0.0f);
            p.setFlySpeed(0.0f);
        }

        if (isPreparationStage() && chosenStartLocation != null) {
            p.teleport(chosenStartLocation);
            setPlayerRespawnPoint(p, chosenStartLocation);
            PotionEffectType sat = resolvePotionEffect("saturation", "SATURATION");
            if (sat != null) {
                p.addPotionEffect(new PotionEffect(sat, 20 * 60 * 60 * 24, 255, false, false, false));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionEffectChange(EntityPotionEffectEvent event) {
        if (gameState != GameState.IN_GAME || gamePaused || longTermStopped) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (speedrunnerUuid == null || !speedrunnerUuid.equals(player.getUniqueId())) return;

        EntityPotionEffectEvent.Action action = event.getAction();
        if (action == EntityPotionEffectEvent.Action.REMOVED || action == EntityPotionEffectEvent.Action.CLEARED) {
            PotionEffect oldEffect = event.getOldEffect();
            if (oldEffect != null && trackedRunnerEffects.remove(oldEffect.getType())) {
                announceEffectExpired(oldEffect.getType());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deceased = event.getEntity();

        if (hunterUuids.contains(deceased.getUniqueId())) {
            event.getDrops().removeIf(this::isHunterCompass);
        }

        if (gameState == GameState.IN_GAME && speedrunnerUuid != null && speedrunnerUuid.equals(deceased.getUniqueId())) {
            Location deathLoc = deceased.getLocation().clone();
            Bukkit.getScheduler().runTaskLater(this, () -> startDeathReplayAndThenHuntersWin(deathLoc), 2L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (isPreparationStage() && chosenStartLocation != null) {
            event.setRespawnLocation(chosenStartLocation);
        } else if (!event.isBedSpawn() && !event.isAnchorSpawn() && chosenStartLocation != null) {
            event.setRespawnLocation(chosenStartLocation);
        }

        if (hunterUuids.contains(player.getUniqueId()) && gameState != GameState.IDLE) {
            Bukkit.getScheduler().runTaskLater(this, () -> giveHunterCompass(player), 1L);
        }
        if (isPreparationStage()) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                PotionEffectType sat = resolvePotionEffect("saturation", "SATURATION");
                if (sat != null) {
                    player.addPotionEffect(new PotionEffect(sat, 20 * 60 * 60 * 24, 255, false, false, false));
                }
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCompassDrop(PlayerDropItemEvent event) {
        if (gamePaused || longTermStopped) {
            event.setCancelled(true);
            return;
        }
        if (hunterUuids.contains(event.getPlayer().getUniqueId())
                && isHunterCompass(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Вы не можете выбросить компас охотника!");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAdvancementDone(PlayerAdvancementDoneEvent event) {
        if (gameState != GameState.IN_GAME) return;
        Player player = event.getPlayer();
        if (speedrunnerUuid == null || !speedrunnerUuid.equals(player.getUniqueId())) return;

        String key = event.getAdvancement().getKey().toString();
        if (key.equals("minecraft:end/kill_dragon") || key.endsWith(":end/kill_dragon")) {
            triggerSpeedrunnerWin();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDragonDeath(EntityDeathEvent event) {
        if (gameState != GameState.IN_GAME) return;
        if (event.getEntity() instanceof EnderDragon) {
            Player killer = event.getEntity().getKiller();
            if (killer != null && speedrunnerUuid != null && speedrunnerUuid.equals(killer.getUniqueId())) {
                triggerSpeedrunnerWin();
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();

        if (longTermStopped) {
            frozenLocationsDuringStop.put(uuid, p.getLocation().clone());
            saveGameStateToDisk();
        }

        p.setWalkSpeed(0.2f);
        p.setFlySpeed(0.1f);
        PotionEffectType slowType = resolvePotionEffect("slowness", "SLOWNESS", "SLOW");
        if (slowType != null) {
            PotionEffect eff = p.getPotionEffect(slowType);
            if (eff != null && eff.getAmplifier() >= 10) {
                p.removePotionEffect(slowType);
            }
        }

        if (readyPlayers.remove(uuid) && gameState == GameState.COUNTDOWN) {
            cancelCountdownAndRestoreBorder();
        }
    }

    // =========================================================================
    // ПОБЕДА СТОРОН
    // =========================================================================

    private void triggerHuntersWinFinal() {
        if (gameState == GameState.ENDED) return;
        longTermStopped = false;
        if (stopBossBar != null) {
            stopBossBar.removeAll();
            stopBossBar = null;
        }
        stopPauseImmediately(true);
        clearSavedGameStateFile();
        gameState = GameState.ENDED;
        updateSidebarScoreboard();

        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");
        Bukkit.broadcastMessage(ChatColor.BLUE + "Спидранер погиб! Победа Охотников (Hunters Wins!!)!");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Итоговое время игры: " + ChatColor.GREEN + formatHhMmSs(elapsedGameSeconds));
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Для полного завершения пропишите: /manhunt game end");
        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGlowing(false);
            restorePlayerMovement(p);
            p.setGameMode(GameMode.SPECTATOR);
            p.sendTitle(ChatColor.BLUE + "Hunters Wins!!", ChatColor.YELLOW + "Спидранер погиб!", 10, 100, 20);
            playSoundSafe(p, SOUND_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    private void triggerSpeedrunnerWin() {
        if (gameState == GameState.ENDED) return;
        longTermStopped = false;
        if (stopBossBar != null) {
            stopBossBar.removeAll();
            stopBossBar = null;
        }
        stopPauseImmediately(true);
        clearSavedGameStateFile();
        gameState = GameState.ENDED;
        updateSidebarScoreboard();

        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");
        Bukkit.broadcastMessage(ChatColor.RED + "Спидранер победил Эндер-дракона и получил достижение «Освободите Энд»!");
        Bukkit.broadcastMessage(ChatColor.GREEN + "Победа Спидранера! Время: " + formatHhMmSs(elapsedGameSeconds));
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Для полного завершения пропишите: /manhunt game end");
        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGlowing(false);
            restorePlayerMovement(p);
            p.setGameMode(GameMode.SPECTATOR);
            p.sendTitle(ChatColor.RED + "Speedrunner Wins!!", ChatColor.GOLD + "Освободите Энд!", 10, 100, 20);
            playSoundSafe(p, SOUND_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    private void triggerSpeedrunnerWinByPortalBreak(String breakerName) {
        if (gameState == GameState.ENDED) return;
        longTermStopped = false;
        if (stopBossBar != null) {
            stopBossBar.removeAll();
            stopBossBar = null;
        }
        stopPauseImmediately(true);
        clearSavedGameStateFile();
        gameState = GameState.ENDED;
        updateSidebarScoreboard();

        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");
        Bukkit.broadcastMessage(ChatColor.RED + "Спидранер выиграл! " + ChatColor.YELLOW + breakerName + " разрушил портал спидранера!");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Для полного завершения пропишите: /manhunt game end");
        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGlowing(false);
            restorePlayerMovement(p);
            p.setGameMode(GameMode.SPECTATOR);
            p.sendTitle(
                    ChatColor.GREEN + "Спидранер выиграл!",
                    ChatColor.RED + breakerName + " разрушил портал!",
                    10, 100, 20
            );
            playSoundSafe(p, SOUND_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    // =========================================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ (КОМПАС, КОМАНДЫ, ЗДОРОВЬЕ, СБРОС)
    // =========================================================================

    private ItemStack createHunterCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS, 1);
        CompassMeta meta = (CompassMeta) compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "Компас Охотника");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Указывает на спидранера",
                    ChatColor.GRAY + "или последний портал его измерения."
            ));
            meta.getPersistentDataContainer().set(compassKey, PersistentDataType.BYTE, (byte) 1);
            compass.setItemMeta(meta);
        }
        return compass;
    }

    private boolean isHunterCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(compassKey, PersistentDataType.BYTE);
    }

    private boolean hasHunterCompass(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isHunterCompass(item)) {
                return true;
            }
        }
        return false;
    }

    private void giveHunterCompass(Player hunter) {
        if (hasHunterCompass(hunter)) {
            return;
        }
        hunter.getInventory().addItem(createHunterCompass());
    }

    private void removeHunterCompass(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (isHunterCompass(contents[i])) {
                player.getInventory().setItem(i, null);
            }
        }
    }

    private void assignPlayerToTeam(Player player, String teamName, ChatColor color) {
        try {
            Scoreboard board = Bukkit.getScoreboardManager() != null
                    ? Bukkit.getScoreboardManager().getMainScoreboard()
                    : null;
            if (board != null) {
                Team team = board.getTeam(teamName);
                if (team != null) {
                    team.addEntry(player.getName());
                }
            }
        } catch (Throwable ignored) {
        }
        player.setDisplayName(color + player.getName() + ChatColor.RESET);
        player.setPlayerListName(color + player.getName());
    }

    private void removePlayerFromScoreboardTeam(Player player) {
        try {
            Scoreboard board = Bukkit.getScoreboardManager() != null
                    ? Bukkit.getScoreboardManager().getMainScoreboard()
                    : null;
            if (board != null) {
                for (Team team : board.getTeams()) {
                    if (team.hasEntry(player.getName())) {
                        team.removeEntry(player.getName());
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
    }

    @SuppressWarnings("deprecation")
    private void setPlayerMaxHealth(Player player, double maxHealth) {
        try {
            Object attr = null;
            try {
                attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("max_health"));
            } catch (Throwable ignored) {
            }
            if (attr == null) {
                try {
                    attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic.max_health"));
                } catch (Throwable ignored) {
                }
            }
            if (attr != null) {
                Method getAttr = player.getClass().getMethod("getAttribute", Class.forName("org.bukkit.attribute.Attribute"));
                AttributeInstance instance = (AttributeInstance) getAttr.invoke(player, attr);
                if (instance != null) {
                    instance.setBaseValue(maxHealth);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }
        try {
            player.setMaxHealth(maxHealth);
        } catch (Throwable ignored) {
        }
    }

    private void resetPlayerHealthAndMode(Player player) {
        try {
            player.setGlowing(false);
            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.1f);
            setPlayerMaxHealth(player, 20.0);
            if (player.getHealth() > 20.0) {
                player.setHealth(20.0);
            }
            player.setGameMode(GameMode.SURVIVAL);
            for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
                player.removePotionEffect(effect.getType());
            }
        } catch (Throwable ignored) {
        }
    }

    private void resetAllPlayerStates() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            resetPlayerHealthAndMode(p);
            removeHunterCompass(p);
            removePlayerFromScoreboardTeam(p);
        }
    }

    @SuppressWarnings("deprecation")
    private void resetInternalState(boolean resetWorldBorders) {
        cancelCountdown();
        if (replayPlaybackTask != null) {
            replayPlaybackTask.cancel();
            replayPlaybackTask = null;
        }
        cleanupReplayEntities();
        longTermStopped = false;
        if (stopBossBar != null) {
            stopBossBar.removeAll();
            stopBossBar = null;
        }
        stopPauseImmediately(true);
        removeWaitingBox();
        resetAllPlayerStates();

        speedrunnerUuid = null;
        hunterUuids.clear();
        readyPlayers.clear();
        selectedDifficulty = null;
        chosenStartLocation = null;
        lastRunnerLocations.clear();
        trackedRunnerEffects.clear();
        runnerOverworldPortalBlocks.clear();
        runnerInitialNetherPortalBlocks.clear();
        runnerInitialNetherPortalCenter = null;
        replayFrames.clear();
        pendingBlockChangesThisTick.clear();
        frozenLocationsDuringStop.clear();
        elapsedGameSeconds = 0L;
        pauseCooldownEndMillis = 0L;
        combatCooldownEndMillis = 0L;

        if (resetWorldBorders) {
            resetWorldBorder(getMainOverworld());
            for (World w : Bukkit.getWorlds()) {
                try {
                    w.setPVP(true);
                    w.setGameRuleValue("doDaylightCycle", "true");
                } catch (Throwable ignored) {
                }
            }
            dispatchConsoleCommandSafe("gamerule doDaylightCycle true");
            setLocatorBarAllWorlds(true);
        }
    }

    private void resetWorldBorder(World world) {
        if (world == null) return;
        try {
            WorldBorder border = world.getWorldBorder();
            border.setCenter(0.0, 0.0);
            border.setSize(DEFAULT_MINECRAFT_BORDER);
            dispatchWorldBorderCommand(world, "center 0 0");
            dispatchWorldBorderCommand(world, "set 30000000");
        } catch (Throwable ignored) {
        }
    }

    private World getMainOverworld() {
        if (activeOverworld != null) {
            return activeOverworld;
        }
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == World.Environment.NORMAL) {
                return w;
            }
        }
        return Bukkit.getWorlds().get(0);
    }

    private World getNetherWorld() {
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == World.Environment.NETHER) {
                return w;
            }
        }
        return null;
    }

    private World getEndWorld() {
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == World.Environment.THE_END) {
                return w;
            }
        }
        return null;
    }

    private void sendDifficultyInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "========== Сложности Asonsss_manhunt ==========");
        sender.sendMessage(ChatColor.GREEN + "1. EASY MODE:");
        sender.sendMessage(ChatColor.GRAY + " • 30 HP у спидранера, мир 10000 блоков, зона выбора спавна 1000x1000.");
        sender.sendMessage(ChatColor.GRAY + " • Команда /fortrest (координаты ближайшей крепости Ада).");
        sender.sendMessage(ChatColor.GRAY + " • Старт (за каждого охотника): Скорость V (+60 сек), Сила II (+10 мин), Регенерация II (+15 мин).");

        sender.sendMessage(ChatColor.YELLOW + "2. MEDIUM MODE:");
        sender.sendMessage(ChatColor.GRAY + " • 30 HP у спидранера, мир 9000 блоков, зона выбора спавна 500x500.");
        sender.sendMessage(ChatColor.GRAY + " • Старт (за каждого охотника): Скорость V (+30 сек), Сила I (+5 мин), Регенерация II (+5 мин).");

        sender.sendMessage(ChatColor.RED + "3. HARD MODE:");
        sender.sendMessage(ChatColor.GRAY + " • 20 HP у спидранера, мир 8000 блоков, зона выбора спавна 500x500.");
        sender.sendMessage(ChatColor.GRAY + " • Старт (за каждого охотника): Скорость V (+15 сек).");

        sender.sendMessage(ChatColor.DARK_PURPLE + "4. IMPOSSIBLE MODE:");
        sender.sendMessage(ChatColor.GRAY + " • 20 HP у спидранера, зона выбора спавна 500x500, без стартовых бустов.");
        sender.sendMessage(ChatColor.GRAY + " • Граница мира отсутсвует.");
        sender.sendMessage(ChatColor.GRAY + " • Через 4 часа спидранер автоматически погибает.");
        sender.sendMessage(ChatColor.GOLD + "===============================================");
    }

    private void sendFullModInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "========== Информация о моде Asonsss_manhunt (v1.5.0) ==========");
        sender.sendMessage(ChatColor.AQUA + "Команды администратора:");
        sender.sendMessage(ChatColor.YELLOW + " • /manhunt game start_prepare (или start) " + ChatColor.GRAY + "— отфильтровать мир без океана у 0:0 и начать подготовку");
        sender.sendMessage(ChatColor.YELLOW + " • /manhunt speedruner add/remove <Игрок> " + ChatColor.GRAY + "— назначить или удалить спидранера");
        sender.sendMessage(ChatColor.YELLOW + " • /manhunt hunter add/remove <Игрок> " + ChatColor.GRAY + "— назначить или удалить охотника");
        sender.sendMessage(ChatColor.YELLOW + " • /manhunt game reset " + ChatColor.GRAY + "— полностью сбросить подготовку, роли и настройки");
        sender.sendMessage(ChatColor.YELLOW + " • /manhunt game end " + ChatColor.GRAY + "— завершить игру, вернуть всех на спавн и сбросить роли");

        sender.sendMessage(ChatColor.RED + "Команды спидранера (по порядку):");
        sender.sendMessage(ChatColor.YELLOW + " • /sp difficulty select <easy|medium|hard|impossible> " + ChatColor.GRAY + "— выбрать сложность игры");
        sender.sendMessage(ChatColor.YELLOW + " • /sp pickplace start " + ChatColor.GRAY + "— полёт в режиме Наблюдателя в пределах зоны спавна");
        sender.sendMessage(ChatColor.YELLOW + " • /sp restart " + ChatColor.GRAY + "— пересоздать мир с новым сидом (без океана в 200 блоках) во время выбора места");
        sender.sendMessage(ChatColor.YELLOW + " • /sp pickplace end " + ChatColor.GRAY + "— зафиксировать место старта и телепортировать всех игроков");
        sender.sendMessage(ChatColor.YELLOW + " • /fortrest " + ChatColor.GRAY + "— узнать координаты ближайшей крепости Ада (только на EASY)");

        sender.sendMessage(ChatColor.BLUE + "Команды охотников:");
        sender.sendMessage(ChatColor.YELLOW + " • /compass (или /c) " + ChatColor.GRAY + "— получить компас охотника во время игры (если его нет в инвентаре)");

        sender.sendMessage(ChatColor.GREEN + "Общие команды для всех игроков:");
        sender.sendMessage(ChatColor.YELLOW + " • /ready / /unready " + ChatColor.GRAY + "— подтвердить / отменить готовность к старту");
        sender.sendMessage(ChatColor.YELLOW + " • /pause <секунды> (или /p <секунды>) " + ChatColor.GRAY + "— заморозить игру (до 180 сек, КД 10 мин, нельзя 1 мин после боя)");
        sender.sendMessage(ChatColor.YELLOW + " • /stop и /resume " + ChatColor.GRAY + "— надолго остановить игру (с сохранением при выходе) и возобновить её");
        sender.sendMessage(ChatColor.YELLOW + " • /manhunt difficulty info " + ChatColor.GRAY + "— подробное описание всех режимов сложности");
        sender.sendMessage(ChatColor.YELLOW + " • /manhunt info " + ChatColor.GRAY + "— показать этот список всех команд плагина");
        sender.sendMessage(ChatColor.GOLD + "================================================================");
    }

    private void sendManhuntHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Команды Asonsss_manhunt (v1.5.0) ===");
        sender.sendMessage(ChatColor.YELLOW + "/manhunt info " + ChatColor.GRAY + "— полное описание всех команд мода");
        sender.sendMessage(ChatColor.YELLOW + "/manhunt difficulty info " + ChatColor.GRAY + "— информация о сложностях");
        sender.sendMessage(ChatColor.YELLOW + "/manhunt game start_prepare (или start) " + ChatColor.GRAY + "— начать подготовку к игре");
        sender.sendMessage(ChatColor.YELLOW + "/manhunt speedruner add/remove <Игрок> " + ChatColor.GRAY + "— назначить/убрать спидранера");
        sender.sendMessage(ChatColor.YELLOW + "/manhunt hunter add/remove <Игрок> " + ChatColor.GRAY + "— назначить/убрать охотника");
        sender.sendMessage(ChatColor.YELLOW + "/manhunt game reset " + ChatColor.GRAY + "— полный сброс подготовки");
        sender.sendMessage(ChatColor.YELLOW + "/manhunt game end " + ChatColor.GRAY + "— завершить игру и сбросить всё");
    }

    // =========================================================================
    // TAB COMPLETER (АВТОДОПОЛНЕНИЕ КОМАНД)
    // =========================================================================

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = command.getName().toLowerCase(Locale.ROOT);

        if (cmd.equals("manhunt")) {
            if (args.length == 1) {
                return filterStartsWith(Arrays.asList("game", "speedruner", "hunter", "difficulty", "info"), args[0]);
            }
            if (args.length == 2) {
                switch (args[0].toLowerCase(Locale.ROOT)) {
                    case "game":
                        return filterStartsWith(Arrays.asList("start_prepare", "start", "reset", "end"), args[1]);
                    case "speedruner":
                    case "speedrunner":
                    case "hunter":
                        return filterStartsWith(Arrays.asList("add", "remove"), args[1]);
                    case "difficulty":
                        return filterStartsWith(Collections.singletonList("info"), args[1]);
                }
            }
            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("speedruner")
                        || args[0].equalsIgnoreCase("speedrunner")
                        || args[0].equalsIgnoreCase("hunter")) {
                    return null;
                }
            }
        }

        if (cmd.equals("sp")) {
            if (args.length == 1) {
                return filterStartsWith(Arrays.asList("difficulty", "pickplace", "restart"), args[0]);
            }
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("difficulty")) {
                    return filterStartsWith(Collections.singletonList("select"), args[1]);
                }
                if (args[0].equalsIgnoreCase("pickplace")) {
                    return filterStartsWith(Arrays.asList("start", "end"), args[1]);
                }
            }
            if (args.length == 3 && args[0].equalsIgnoreCase("difficulty") && args[1].equalsIgnoreCase("select")) {
                return filterStartsWith(Arrays.asList("easy", "medium", "hard", "impossible"), args[2]);
            }
        }

        if (cmd.equals("pause")) {
            if (args.length == 1) {
                return filterStartsWith(Arrays.asList("30", "60", "120", "180"), args[0]);
            }
        }

        return Collections.emptyList();
    }

    private List<String> filterStartsWith(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(opt -> opt.toLowerCase(Locale.ROOT).startsWith(lower))
                .collect(Collectors.toList());
    }
}
