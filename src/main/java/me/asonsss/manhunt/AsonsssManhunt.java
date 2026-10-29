package me.asonsss.manhunt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.StructureType;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Полный исходный код плагина: Asonsss_manhunt (Версия 1.3.0)
 *
 * ======================== 1. ФАЙЛ src/main/resources/plugin.yml ========================
 * name: Asonsss_manhunt
 * version: '1.3.0'
 * main: me.asonsss.manhunt.AsonsssManhunt
 * api-version: '1.21'
 * description: Manhunt plugin v1.3.0 with waiting barrier box, combat pause lock, compass command, scoreboard and dimensional tracking
 * commands:
 *   manhunt:
 *     description: Основная команда управления Manhunt
 *     aliases: [speedrun]
 *     usage: /manhunt <game|speedruner|hunter|difficulty|info> ...
 *   sp:
 *     description: Команды спидранера для настройки сложности и выбора места старта
 *     usage: /sp <difficulty|pickplace> ...
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
 *
 * ======================== 2. ФАЙЛ pom.xml (для сборки через Maven) ========================
 * <project xmlns="http://maven.apache.org/POM/4.0.0"
 *          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
 *     <modelVersion>4.0.0</modelVersion>
 *     <groupId>me.asonsss</groupId>
 *     <artifactId>Asonsss_manhunt</artifactId>
 *     <version>1.3.0</version>
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
        ENDED
    }

    public enum DifficultyMode {
        EASY("EASY", ChatColor.GREEN, 30.0, 10000.0, 1000.0),
        MEDIUM("MEDIUM", ChatColor.YELLOW, 30.0, 9000.0, 500.0),
        HARD("HARD", ChatColor.RED, 20.0, 8000.0, 500.0),
        IMPOSSIBLE("IMPOSSIBLE", ChatColor.DARK_PURPLE, 20.0, 60000000.0, 500.0);

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

    private static final String TEAM_SPEEDRUNNER = "mh_speedrunner";
    private static final String TEAM_HUNTERS = "mh_hunters";
    private static final String OBJECTIVE_SIDEBAR = "mh_sidebar";
    private static final double PRE_GAME_BORDER_SIZE = 30.0;
    private static final double DEFAULT_MINECRAFT_BORDER = 60000000.0; // Стандартная граница (радиус 30 млн блоков)
    private static final long IMPOSSIBLE_TIME_LIMIT_SECONDS = 4L * 60L * 60L; // 4 часа = 14400 секунд
    private static final int MAX_PAUSE_SECONDS = 180;
    private static final long PAUSE_COOLDOWN_MILLIS = 10L * 60L * 1000L; // 10 минут
    private static final long COMBAT_PAUSE_LOCK_MILLIS = 60L * 1000L; // 1 минута после удара

    // Координаты коробки ожидания 20x20x20 из барьеров на высоте 250 вокруг (0, 0)
    private static final int BOX_MIN_X = -10;
    private static final int BOX_MAX_X = 9;
    private static final int BOX_MIN_Y = 250;
    private static final int BOX_MAX_Y = 269;
    private static final int BOX_MIN_Z = -10;
    private static final int BOX_MAX_Z = 9;

    private GameState gameState = GameState.IDLE;
    private DifficultyMode selectedDifficulty = null;

    private UUID speedrunnerUuid = null;
    private final Set<UUID> hunterUuids = new HashSet<>();
    private final Set<UUID> readyPlayers = new HashSet<>();

    private final Map<World.Environment, Location> lastRunnerLocations = new EnumMap<>(World.Environment.class);
    private final Set<PotionEffectType> trackedRunnerEffects = new HashSet<>();

    private World activeOverworld = null;
    private Location chosenStartLocation = null;
    private boolean waitingBoxBuilt = false;

    private BukkitTask countdownTask = null;
    private BukkitTask gameLoopTask = null;
    private BukkitTask scoreboardAndTimerTask = null;

    // Таймер игры / обратного отсчёта IMPOSSIBLE (в секундах)
    private long elapsedGameSeconds = 0L;
    private boolean scoreboardActive = false;

    // Переменные системы паузы (/pause, /p) и боевого таймера
    private boolean prePauseActive = false;
    private boolean gamePaused = false;
    private long pauseCooldownEndMillis = 0L;
    private long combatCooldownEndMillis = 0L;
    private BukkitTask pauseTask = null;
    private BossBar pauseBossBar = null;
    private final Map<UUID, Collection<PotionEffect>> savedEffectsDuringPause = new HashMap<>();

    private NamespacedKey compassKey;

    @Override
    public void onEnable() {
        this.compassKey = new NamespacedKey(this, "hunter_compass");

        Bukkit.getPluginManager().registerEvents(this, this);

        registerCommand("manhunt");
        registerCommand("sp");
        registerCommand("ready");
        registerCommand("unready");
        registerCommand("fortrest");
        registerCommand("pause");
        registerCommand("compass");

        setupScoreboardTeams();
        startCompassAndEffectTask();
        startScoreboardAndTimerTask();

        getLogger().info("Asonsss_manhunt v1.3.0 успешно запущен!");
    }

    @Override
    public void onDisable() {
        cancelCountdown();
        stopPauseImmediately(false);
        removeWaitingBox();
        if (gameLoopTask != null) {
            gameLoopTask.cancel();
            gameLoopTask = null;
        }
        if (scoreboardAndTimerTask != null) {
            scoreboardAndTimerTask.cancel();
            scoreboardAndTimerTask = null;
        }
        clearSidebarScoreboard();
        resetAllPlayerStates();
    }

    private void registerCommand(String name) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }
    }

    private void setupScoreboardTeams() {
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
    }

    // =========================================================================
    // ОБРАБОТКА КОМАНД
    // =========================================================================

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmdName = command.getName().toLowerCase(Locale.ROOT);

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
            default:
                return false;
        }
    }

    private boolean handleManhuntCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendManhuntHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        // /manhunt info — показывает все команды мода и что они делают (доступна всем)
        if (sub.equals("info")) {
            sendFullModInfo(sender);
            return true;
        }

        // /manhunt difficulty info — доступна любому игроку в любое время
        if (sub.equals("difficulty")) {
            if (args.length >= 2 && args[1].equalsIgnoreCase("info")) {
                sendDifficultyInfo(sender);
                return true;
            }
            sender.sendMessage(ChatColor.RED + "Использование: /manhunt difficulty info");
            return true;
        }

        // Остальные подкоманды /manhunt требуют прав администратора (OP)
        if (!sender.isOp() && !sender.hasPermission("manhunt.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав администратора для выполнения этой команды.");
            return true;
        }

        if (sub.equals("game")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Использование: /manhunt game <start_prepare|reset|end>");
                return true;
            }
            String action = args[1].toLowerCase(Locale.ROOT);
            switch (action) {
                case "start_prepare":
                    boolean createNewWorld = args.length >= 3 && args[2].equalsIgnoreCase("new_world");
                    startPreparePhase(sender, createNewWorld);
                    return true;
                case "reset":
                    resetGameFull(sender);
                    return true;
                case "end":
                    endGameCleanup(sender);
                    return true;
                default:
                    sender.sendMessage(ChatColor.RED + "Неизвестное действие. Используйте: start_prepare, reset или end.");
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

        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Доступные команды спидранера (в строгой последовательности):");
            player.sendMessage(ChatColor.GRAY + "1. /sp difficulty select <easy|medium|hard|impossible>");
            player.sendMessage(ChatColor.GRAY + "2. /sp pickplace start");
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
                cancelCountdown();
                gameState = GameState.WAITING_READY;
                Bukkit.broadcastMessage(ChatColor.RED + "[Manhunt] Обратный отсчёт отменён!");
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

        Location fortressLoc = world.locateNearestStructure(origin, StructureType.NETHER_FORTRESS, 100, false);
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
    // НОВАЯ КОМАНДА /compass (/c) ДЛЯ ОХОТНИКОВ
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
    // МЕХАНИКА ПАУЗЫ (/pause и /p) С УЧЁТОМ СХВАТКИ
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

        if (prePauseActive || gamePaused) {
            player.sendMessage(ChatColor.RED + "Пауза уже активна или готовится к запуску!");
            return true;
        }

        long now = System.currentTimeMillis();

        // 1. Сначала проверяем общий 10-минутный кулдаун от прошлого использования паузы
        if (now < pauseCooldownEndMillis) {
            long remainingSeconds = Math.max(1L, (pauseCooldownEndMillis - now + 999L) / 1000L);
            player.sendMessage(ChatColor.RED + "Команда станет снова доступна через " + formatCooldownTime(remainingSeconds) + ".");
            return true;
        }

        // 2. Если общего КД нет, проверяем 1-минутный запрет после удара между спидранером и охотником
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
                if (gameState != GameState.IN_GAME) {
                    stopPauseImmediately(true);
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
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
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
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
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
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick freeze");

        savedEffectsDuringPause.clear();
        PotionEffectType slowType = resolvePotionEffect("SLOWNESS", "SLOW");
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
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
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
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick unfreeze");
            } catch (Exception ignored) {
            }

            PotionEffectType slowType = resolvePotionEffect("SLOWNESS", "SLOW");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setWalkSpeed(0.2f);
                p.setFlySpeed(0.1f);
                if (slowType != null) {
                    p.removePotionEffect(slowType);
                }
                Collection<PotionEffect> saved = savedEffectsDuringPause.get(p.getUniqueId());
                if (saved != null) {
                    for (PotionEffect eff : saved) {
                        p.addPotionEffect(eff);
                    }
                }
            }
            savedEffectsDuringPause.clear();
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
    // SCOREBOARD СПРАВА И ТАЙМЕР ИГРЫ (С УСЛОВИЕМ IMPOSSIBLE 4 ЧАСА)
    // =========================================================================

    private void startScoreboardAndTimerTask() {
        scoreboardAndTimerTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Во время подготовки удерживаем время мира на 0 и сытость игроков
                if (isPreparationStage() || gameState == GameState.COUNTDOWN) {
                    World overworld = getMainOverworld();
                    if (overworld != null) {
                        overworld.setTime(0L);
                    }
                }

                if (gameState == GameState.IN_GAME && !gamePaused) {
                    elapsedGameSeconds++;

                    // На сложности IMPOSSIBLE через 4 часа спидранер умирает
                    if (selectedDifficulty == DifficultyMode.IMPOSSIBLE
                            && elapsedGameSeconds >= IMPOSSIBLE_TIME_LIMIT_SECONDS) {
                        Player runner = speedrunnerUuid != null ? Bukkit.getPlayer(speedrunnerUuid) : null;
                        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "[Manhunt] Истекли 4 часа в режиме IMPOSSIBLE! Спидранер погибает!");
                        if (runner != null && runner.isOnline() && !runner.isDead()) {
                            runner.setHealth(0.0);
                        } else {
                            triggerHuntersWin();
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

        // Цвет выбранной сложности такой же, как в /manhunt difficulty info
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
    }

    private void clearSidebarScoreboard() {
        scoreboardActive = false;
        if (Bukkit.getScoreboardManager() == null) return;
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective obj = board.getObjective(OBJECTIVE_SIDEBAR);
        if (obj != null) {
            obj.unregister();
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
        return new Location(world, 0.5, BOX_MIN_Y + 1.0, 0.5);
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

    private void applyPreparationWorldRules(World overworld, Location boxSpawn) {
        for (World w : Bukkit.getWorlds()) {
            w.setPVP(false);
            w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            w.setTime(0L);
        }
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "time set 0");
        } catch (Exception ignored) {
        }

        overworld.setSpawnLocation(boxSpawn.getBlockX(), boxSpawn.getBlockY(), boxSpawn.getBlockZ());
        chosenStartLocation = boxSpawn;

        PotionEffectType saturationType = resolvePotionEffect("SATURATION");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.SURVIVAL);
            p.teleport(boxSpawn);
            p.setBedSpawnLocation(boxSpawn, true);
            p.setFoodLevel(20);
            p.setSaturation(20f);
            if (saturationType != null) {
                p.addPotionEffect(new PotionEffect(saturationType, Integer.MAX_VALUE, 0, false, false, false));
            }
        }
    }

    private void applyActiveGameWorldRules() {
        PotionEffectType saturationType = resolvePotionEffect("SATURATION");
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (saturationType != null) {
                p.removePotionEffect(saturationType);
            }
        }

        for (World w : Bukkit.getWorlds()) {
            w.setPVP(true);
            w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            setLocatorBarRule(w, false);
        }
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule locatorBar false");
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setLocatorBarRule(World world, boolean enabled) {
        try {
            GameRule rule = GameRule.getByName("locatorBar");
            if (rule != null && rule.getType() == Boolean.class) {
                world.setGameRule(rule, enabled);
            }
        } catch (Exception ignored) {
        }
    }

    // =========================================================================
    // ЭТАП 1: УПРАВЛЕНИЕ ПОДГОТОВКОЙ И РОЛЯМИ
    // =========================================================================

    private boolean isPreparationStage() {
        return gameState == GameState.PREPARE
                || gameState == GameState.DIFFICULTY_SELECTED
                || gameState == GameState.PICKING_PLACE
                || gameState == GameState.WAITING_READY;
    }

    private void startPreparePhase(CommandSender sender, boolean createNewWorld) {
        resetInternalState(false);
        gameState = GameState.PREPARE;
        elapsedGameSeconds = 0L;
        scoreboardActive = true;

        if (createNewWorld) {
            String worldName = "manhunt_world_" + (System.currentTimeMillis() / 1000L);
            sender.sendMessage(ChatColor.YELLOW + "Генерация нового мира " + worldName + "...");
            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(World.Environment.NORMAL);
            creator.type(WorldType.NORMAL);
            activeOverworld = Bukkit.createWorld(creator);
        } else {
            activeOverworld = getMainOverworld();
        }

        // Строим коробку ожидания 20x20x20 из барьеров на (0, 250, 0) и переносим всех туда
        Location boxSpawn = buildWaitingBox(activeOverworld);
        applyPreparationWorldRules(activeOverworld, boxSpawn);

        updateSidebarScoreboard();

        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");
        Bukkit.broadcastMessage(ChatColor.GREEN + "Подготовка к Manhunt начата!");
        Bukkit.broadcastMessage(ChatColor.AQUA + "Все игроки перенесены в комнату ожидания (ПВП и голод отключены).");
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
            PotionEffectType sat = resolvePotionEffect("SATURATION");
            if (sat != null) {
                target.addPotionEffect(new PotionEffect(sat, Integer.MAX_VALUE, 0, false, false, false));
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
        gameState = GameState.IDLE;
        Bukkit.broadcastMessage(ChatColor.GOLD + "[Manhunt] " + ChatColor.RED
                + "Весь процесс подготовки и настройки игры полностью сброшены администратором.");
    }

    private void endGameCleanup(CommandSender sender) {
        cancelCountdown();
        stopPauseImmediately(true);
        removeWaitingBox();
        clearSidebarScoreboard();

        World overworld = getMainOverworld();
        int surfaceY = getHighestSurfaceYIgnoringBarriers(overworld, 0, 0) + 1;
        Location defaultSpawn = new Location(overworld, 0.5, surfaceY, 0.5);
        overworld.setSpawnLocation(0, surfaceY, 0);

        resetWorldBorder(overworld);
        for (World w : Bukkit.getWorlds()) {
            w.setPVP(true);
            w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGlowing(false);
            removePlayerFromScoreboardTeam(p);
            removeHunterCompass(p);
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            setPlayerMaxHealth(p, 20.0);
            p.setGameMode(GameMode.SURVIVAL);
            p.setBedSpawnLocation(defaultSpawn, true);
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

        int highestY = getHighestSurfaceYIgnoringBarriers(overworld, 0, 0);
        Location centerLoc = new Location(overworld, 0.5, highestY + 10.0, 0.5);
        speedrunner.teleport(centerLoc);

        speedrunner.setGameMode(GameMode.SPECTATOR);
        speedrunner.setAllowFlight(true);
        speedrunner.setFlying(true);

        int zoneSize = (int) selectedDifficulty.getPickPlaceBorderSize();
        speedrunner.sendMessage(ChatColor.GREEN + "Вы переведены в режим Наблюдателя в зоне "
                + zoneSize + "x" + zoneSize + " блоков от (0, 0).");
        speedrunner.sendMessage(ChatColor.YELLOW + "Выберите место старта и введите: " + ChatColor.GOLD + "/sp pickplace end");
    }

    private void endPickPlace(Player speedrunner) {
        Location runnerLoc = speedrunner.getLocation();
        World world = runnerLoc.getWorld() != null ? runnerLoc.getWorld() : getMainOverworld();

        // Убираем коробку ожидания из барьеров перед расчётом самого верхнего блока поверхности
        removeWaitingBox();

        double exactX = runnerLoc.getX();
        double exactZ = runnerLoc.getZ();
        int blockX = runnerLoc.getBlockX();
        int blockZ = runnerLoc.getBlockZ();
        int topY = getHighestSurfaceYIgnoringBarriers(world, blockX, blockZ) + 1;

        chosenStartLocation = new Location(world, exactX, topY, exactZ, runnerLoc.getYaw(), runnerLoc.getPitch());
        world.setSpawnLocation(blockX, topY, blockZ);

        PotionEffectType saturationType = resolvePotionEffect("SATURATION");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(chosenStartLocation);
            p.setBedSpawnLocation(chosenStartLocation, true);
            if (!p.getUniqueId().equals(speedrunnerUuid)) {
                p.setGameMode(GameMode.SURVIVAL);
                setPlayerMaxHealth(p, 20.0);
                p.setHealth(20.0);
                p.setFoodLevel(20);
            }
            if (saturationType != null) {
                p.addPotionEffect(new PotionEffect(saturationType, Integer.MAX_VALUE, 0, false, false, false));
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

        countdownTask = new BukkitRunnable() {
            int secondsLeft = 15;

            @Override
            public void run() {
                if (secondsLeft > 0) {
                    ChatColor color = secondsLeft <= 3 ? ChatColor.RED
                            : (secondsLeft <= 5 ? ChatColor.GOLD : ChatColor.GREEN);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(color + String.valueOf(secondsLeft),
                                ChatColor.YELLOW + "Приготовьтесь к старту!", 0, 25, 5);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
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

    private void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }

    // =========================================================================
    // ЭТАП 3: НАЧАЛО И ХОД ИГРЫ
    // =========================================================================

    private void beginActiveGame() {
        gameState = GameState.IN_GAME;
        elapsedGameSeconds = 0L;
        pauseCooldownEndMillis = 0L;
        combatCooldownEndMillis = 0L;

        removeWaitingBox();
        // Включаем ПВП, возобновляем смену суток, снимаем эффект сытости и отключаем locatorBar
        applyActiveGameWorldRules();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGlowing(false);
            p.sendTitle(ChatColor.GREEN + "ИГРА НАЧАЛАСЬ!", ChatColor.YELLOW + "Удачи!", 5, 40, 10);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        }
        readyPlayers.clear();

        World world = chosenStartLocation != null && chosenStartLocation.getWorld() != null
                ? chosenStartLocation.getWorld()
                : getMainOverworld();
        WorldBorder border = world.getWorldBorder();
        if (chosenStartLocation != null) {
            border.setCenter(chosenStartLocation.getX(), chosenStartLocation.getZ());
        }

        if (selectedDifficulty == DifficultyMode.IMPOSSIBLE) {
            border.setCenter(0.0, 0.0);
            border.setSize(DEFAULT_MINECRAFT_BORDER);
            Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "[Manhunt] Режим IMPOSSIBLE: у спидранера есть ровно 4 часа, чтобы победить дракона!");
        } else if (selectedDifficulty != null) {
            border.setSize(selectedDifficulty.getGameBorderSize());
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

        PotionEffectType speedType = resolvePotionEffect("SPEED");
        PotionEffectType strengthType = resolvePotionEffect("STRENGTH", "INCREASE_DAMAGE");
        PotionEffectType regenType = resolvePotionEffect("REGENERATION");

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
            PotionEffectType byName = PotionEffectType.getByName(name);
            if (byName != null) {
                return byName;
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
                if (gamePaused) {
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
        String name = type.getName().toUpperCase(Locale.ROOT);
        if (name.contains("SPEED")) return "Скорость";
        if (name.contains("DAMAGE") || name.contains("STRENGTH")) return "Сила";
        if (name.contains("REGENERATION")) return "Регенерация";
        return type.getName();
    }

    // =========================================================================
    // СОБЫТИЯ ИГРЫ, СХВАТКИ И ЗАМОРОЗКИ (LISTENERS)
    // =========================================================================

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (gamePaused) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (to != null && (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ())) {
                event.setTo(from);
            }
            return;
        }

        if (gameState != GameState.IN_GAME) return;
        Player player = event.getPlayer();
        if (speedrunnerUuid != null && speedrunnerUuid.equals(player.getUniqueId())) {
            Location to = event.getTo();
            if (to != null && to.getWorld() != null) {
                lastRunnerLocations.put(to.getWorld().getEnvironment(), to.clone());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (gamePaused) {
            event.setCancelled(true);
            return;
        }

        // На этапе подготовки ПВП полностью отключено
        if (isPreparationStage() || gameState == GameState.COUNTDOWN) {
            if (event.getEntity() instanceof Player) {
                Player attacker = resolveAttackerPlayer(event.getDamager());
                if (attacker != null) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Отслеживание ударов между охотником и спидранером во время игры (блокировка /pause на 1 минуту)
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
    public void onBlockBreak(BlockBreakEvent event) {
        if (gamePaused) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (gamePaused) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (gamePaused) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (gamePaused) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (gamePaused || isPreparationStage() || gameState == GameState.COUNTDOWN) {
            event.setCancelled(true);
            if (event.getEntity() instanceof Player) {
                ((Player) event.getEntity()).setFoodLevel(20);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (scoreboardActive && Bukkit.getScoreboardManager() != null) {
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        if (pauseBossBar != null) {
            pauseBossBar.addPlayer(p);
        }
        if (isPreparationStage() && chosenStartLocation != null) {
            p.teleport(chosenStartLocation);
            p.setBedSpawnLocation(chosenStartLocation, true);
            PotionEffectType sat = resolvePotionEffect("SATURATION");
            if (sat != null) {
                p.addPotionEffect(new PotionEffect(sat, Integer.MAX_VALUE, 0, false, false, false));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        if (speedrunnerUuid != null && speedrunnerUuid.equals(player.getUniqueId())) {
            Location from = event.getFrom();
            if (from.getWorld() != null) {
                lastRunnerLocations.put(from.getWorld().getEnvironment(), from.clone());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (speedrunnerUuid != null && speedrunnerUuid.equals(player.getUniqueId())) {
            Location current = player.getLocation();
            if (current.getWorld() != null) {
                lastRunnerLocations.put(current.getWorld().getEnvironment(), current.clone());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionEffectChange(EntityPotionEffectEvent event) {
        if (gameState != GameState.IN_GAME || gamePaused) return;
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
            triggerHuntersWin();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!event.isBedSpawn() && !event.isAnchorSpawn() && chosenStartLocation != null) {
            event.setRespawnLocation(chosenStartLocation);
        }

        if (hunterUuids.contains(player.getUniqueId()) && gameState != GameState.IDLE) {
            Bukkit.getScheduler().runTaskLater(this, () -> giveHunterCompass(player), 1L);
        }
        if (isPreparationStage()) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                PotionEffectType sat = resolvePotionEffect("SATURATION");
                if (sat != null) {
                    player.addPotionEffect(new PotionEffect(sat, Integer.MAX_VALUE, 0, false, false, false));
                }
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCompassDrop(PlayerDropItemEvent event) {
        if (gamePaused) {
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
        UUID uuid = event.getPlayer().getUniqueId();
        if (readyPlayers.remove(uuid) && gameState == GameState.COUNTDOWN) {
            cancelCountdown();
            gameState = GameState.WAITING_READY;
            Bukkit.broadcastMessage(ChatColor.RED + "[Manhunt] Обратный отсчёт отменён из-за выхода игрока.");
        }
    }

    // =========================================================================
    // ПОБЕДА СТОРОН
    // =========================================================================

    private void triggerHuntersWin() {
        if (gameState == GameState.ENDED) return;
        stopPauseImmediately(true);
        gameState = GameState.ENDED;
        updateSidebarScoreboard();

        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");
        Bukkit.broadcastMessage(ChatColor.BLUE + "Спидранер погиб! Победа Охотников (Hunters Wins!!)!");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Итоговое время игры: " + ChatColor.GREEN + formatHhMmSs(elapsedGameSeconds));
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Для полного завершения пропишите: /manhunt game end");
        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGlowing(false);
            p.setGameMode(GameMode.SPECTATOR);
            p.sendTitle(ChatColor.BLUE + "Hunters Wins!!", ChatColor.YELLOW + "Спидранер погиб!", 10, 100, 20);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    private void triggerSpeedrunnerWin() {
        if (gameState == GameState.ENDED) return;
        stopPauseImmediately(true);
        gameState = GameState.ENDED;
        updateSidebarScoreboard();

        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");
        Bukkit.broadcastMessage(ChatColor.RED + "Спидранер победил Эндер-дракона и получил достижение «Освободите Энд»!");
        Bukkit.broadcastMessage(ChatColor.GREEN + "Победа Спидранера! Время: " + formatHhMmSs(elapsedGameSeconds));
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Для полного завершения пропишите: /manhunt game end");
        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGlowing(false);
            p.setGameMode(GameMode.SPECTATOR);
            p.sendTitle(ChatColor.RED + "Speedrunner Wins!!", ChatColor.GOLD + "Освободите Энд!", 10, 100, 20);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
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
        Scoreboard board = Bukkit.getScoreboardManager() != null
                ? Bukkit.getScoreboardManager().getMainScoreboard()
                : null;
        if (board != null) {
            Team team = board.getTeam(teamName);
            if (team != null) {
                team.addEntry(player.getName());
            }
        }
        player.setDisplayName(color + player.getName() + ChatColor.RESET);
        player.setPlayerListName(color + player.getName());
    }

    private void removePlayerFromScoreboardTeam(Player player) {
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
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
    }

    private void setPlayerMaxHealth(Player player, double maxHealth) {
        Attribute maxHealthAttr = null;
        for (Attribute attr : Attribute.values()) {
            if (attr.name().equals("GENERIC_MAX_HEALTH") || attr.name().equals("MAX_HEALTH")) {
                maxHealthAttr = attr;
                break;
            }
        }
        if (maxHealthAttr != null) {
            AttributeInstance instance = player.getAttribute(maxHealthAttr);
            if (instance != null) {
                instance.setBaseValue(maxHealth);
            }
        }
    }

    private void resetPlayerHealthAndMode(Player player) {
        player.setGlowing(false);
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        setPlayerMaxHealth(player, 20.0);
        if (player.getHealth() > 20.0) {
            player.setHealth(20.0);
        }
        player.setGameMode(GameMode.SURVIVAL);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    private void resetAllPlayerStates() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            resetPlayerHealthAndMode(p);
            removeHunterCompass(p);
            removePlayerFromScoreboardTeam(p);
        }
    }

    private void resetInternalState(boolean resetWorldBorders) {
        cancelCountdown();
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
        elapsedGameSeconds = 0L;
        pauseCooldownEndMillis = 0L;
        combatCooldownEndMillis = 0L;

        if (resetWorldBorders) {
            resetWorldBorder(getMainOverworld());
            for (World w : Bukkit.getWorlds()) {
                w.setPVP(true);
                w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            }
        }
    }

    private void resetWorldBorder(World world) {
        if (world == null) return;
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0.0, 0.0);
        border.setSize(DEFAULT_MINECRAFT_BORDER);
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
        sender.sendMessage(ChatColor.GOLD + "========== Информация о моде Asonsss_manhunt (v1.3.0) ==========");
        sender.sendMessage(ChatColor.AQUA + "Команды администратора:");
        sender.sendMessage(ChatColor.YELLOW + " • /manhunt game start_prepare " + ChatColor.GRAY + "— запустить подготовку, создать комнату ожидания и включить Scoreboard");
        sender.sendMessage(ChatColor.YELLOW + " • /manhunt speedruner add/remove <Игрок> " + ChatColor.GRAY + "— назначить или удалить спидранера");
        sender.sendMessage(ChatColor.YELLOW + " • /manhunt hunter add/remove <Игрок> " + ChatColor.GRAY + "— назначить или удалить охотника");
        sender.sendMessage(ChatColor.YELLOW + " • /manhunt game reset " + ChatColor.GRAY + "— полностью сбросить подготовку, роли и настройки");
        sender.sendMessage(ChatColor.YELLOW + " • /manhunt game end " + ChatColor.GRAY + "— завершить игру, вернуть всех на спавн и сбросить роли");

        sender.sendMessage(ChatColor.RED + "Команды спидранера (по порядку):");
        sender.sendMessage(ChatColor.YELLOW + " • /sp difficulty select <easy|medium|hard|impossible> " + ChatColor.GRAY + "— выбрать сложность игры");
        sender.sendMessage(ChatColor.YELLOW + " • /sp pickplace start " + ChatColor.GRAY + "— полёт в режиме Наблюдателя для выбора места старта");
        sender.sendMessage(ChatColor.YELLOW + " • /sp pickplace end " + ChatColor.GRAY + "— зафиксировать место старта и телепортировать всех игроков");
        sender.sendMessage(ChatColor.YELLOW + " • /fortrest " + ChatColor.GRAY + "— узнать координаты ближайшей крепости Ада (только на EASY)");

        sender.sendMessage(ChatColor.BLUE + "Команды охотников:");
        sender.sendMessage(ChatColor.YELLOW + " • /compass (или /c) " + ChatColor.GRAY + "— получить компас охотника во время игры (если его нет в инвентаре)");

        sender.sendMessage(ChatColor.GREEN + "Общие команды для всех игроков:");
        sender.sendMessage(ChatColor.YELLOW + " • /ready " + ChatColor.GRAY + "— подтвердить готовность к началу игры (включает свечение)");
        sender.sendMessage(ChatColor.YELLOW + " • /unready " + ChatColor.GRAY + "— отменить готовность к началу игры");
        sender.sendMessage(ChatColor.YELLOW + " • /pause <секунды> (или /p <секунды>) " + ChatColor.GRAY + "— заморозить игру (до 180 сек, КД 10 мин, нельзя 1 мин после боя)");
        sender.sendMessage(ChatColor.YELLOW + " • /manhunt difficulty info " + ChatColor.GRAY + "— подробное описание всех режимов сложности");
        sender.sendMessage(ChatColor.YELLOW + " • /manhunt info " + ChatColor.GRAY + "— показать этот список всех команд плагина");
        sender.sendMessage(ChatColor.GOLD + "================================================================");
    }

    private void sendManhuntHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Команды Asonsss_manhunt (v1.3.0) ===");
        sender.sendMessage(ChatColor.YELLOW + "/manhunt info " + ChatColor.GRAY + "— полное описание всех команд мода");
        sender.sendMessage(ChatColor.YELLOW + "/manhunt difficulty info " + ChatColor.GRAY + "— информация о сложностях");
        sender.sendMessage(ChatColor.YELLOW + "/manhunt game start_prepare " + ChatColor.GRAY + "— начать подготовку к игре");
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
                        return filterStartsWith(Arrays.asList("start_prepare", "reset", "end"), args[1]);
                    case "speedruner":
                    case "speedrunner":
                    case "hunter":
                        return filterStartsWith(Arrays.asList("add", "remove"), args[1]);
                    case "difficulty":
                        return filterStartsWith(Collections.singletonList("info"), args[1]);
                }
            }
            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("game") && args[1].equalsIgnoreCase("start_prepare")) {
                    return filterStartsWith(Collections.singletonList("new_world"), args[2]);
                }
                if (args[0].equalsIgnoreCase("speedruner")
                        || args[0].equalsIgnoreCase("speedrunner")
                        || args[0].equalsIgnoreCase("hunter")) {
                    return null;
                }
            }
        }

        if (cmd.equals("sp")) {
            if (args.length == 1) {
                return filterStartsWith(Arrays.asList("difficulty", "pickplace"), args[0]);
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