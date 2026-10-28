package me.asonsss.manhunt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Полный исходный код плагина: Asonsss_manhunt (Версия 26.3)
 *
 * ======================== 1. ФАЙЛ src/main/resources/plugin.yml ========================
 * name: Asonsss_manhunt
 * version: '26.3'
 * main: me.asonsss.manhunt.AsonsssManhunt
 * api-version: '1.21'
 * description: Manhunt plugin with difficulty modes, spawn picker, ready aura and dimensional compass
 * commands:
 *   manhunt:
 *     description: Основная команда управления Manhunt
 *     usage: /manhunt <game|speedruner|hunter|difficulty> ...
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
 *
 * ======================== 2. ФАЙЛ pom.xml (для сборки через Maven) ========================
 * <project xmlns="http://maven.apache.org/POM/4.0.0"
 *          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
 *     <modelVersion>4.0.0</modelVersion>
 *     <groupId>me.asonsss</groupId>
 *     <artifactId>Asonsss_manhunt</artifactId>
 *     <version>26.3</version>
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
        EASY("EASY", 30.0, 10000.0, 1000.0),
        MEDIUM("MEDIUM", 30.0, 9000.0, 500.0),
        HARD("HARD", 20.0, 8000.0, 500.0),
        IMPOSSIBLE("IMPOSSIBLE", 20.0, 60000.0, 500.0);

        private final String displayName;
        private final double speedrunnerMaxHealth;
        private final double gameBorderSize;
        private final double pickPlaceBorderSize;

        DifficultyMode(String displayName, double speedrunnerMaxHealth, double gameBorderSize, double pickPlaceBorderSize) {
            this.displayName = displayName;
            this.speedrunnerMaxHealth = speedrunnerMaxHealth;
            this.gameBorderSize = gameBorderSize;
            this.pickPlaceBorderSize = pickPlaceBorderSize;
        }

        public String getDisplayName() {
            return displayName;
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
    private static final double PRE_GAME_BORDER_SIZE = 30.0;
    private static final long IMPOSSIBLE_SHRINK_SECONDS = 4L * 60L * 60L; // 4 часа = 14400 секунд

    private GameState gameState = GameState.IDLE;
    private DifficultyMode selectedDifficulty = null;

    private UUID speedrunnerUuid = null;
    private final Set<UUID> hunterUuids = new HashSet<>();
    private final Set<UUID> readyPlayers = new HashSet<>();

    private final Map<World.Environment, Location> lastRunnerLocations = new EnumMap<>(World.Environment.class);
    private final Set<PotionEffectType> trackedRunnerEffects = new HashSet<>();

    private World activeOverworld = null;
    private Location chosenStartLocation = null;
    private BukkitTask countdownTask = null;
    private BukkitTask gameLoopTask = null;

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

        setupScoreboardTeams();
        startCompassAndEffectTask();

        getLogger().info("Asonsss_manhunt v26.3 успешно запущен!");
    }

    @Override
    public void onDisable() {
        cancelCountdown();
        if (gameLoopTask != null) {
            gameLoopTask.cancel();
            gameLoopTask = null;
        }
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

            Bukkit.broadcastMessage(ChatColor.GOLD + "[Manhunt] " + ChatColor.RED + player.getName()
                    + ChatColor.YELLOW + " выбрал сложность: " + ChatColor.AQUA + mode.getDisplayName());
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

        // Если спидранер находится в Верхнем мире, пересчитываем его координаты для Незера (1:8)
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

        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");
        Bukkit.broadcastMessage(ChatColor.GREEN + "Подготовка к Manhunt начата!");
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
        removePlayerFromScoreboardTeam(target);

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

        sender.sendMessage(ChatColor.GREEN + "Игрок " + target.getName() + " удалён из команды охотников.");
        target.sendMessage(ChatColor.YELLOW + "С вас снята роль охотника.");
    }

    private void resetGameFull(CommandSender sender) {
        resetInternalState(true);
        gameState = GameState.IDLE;
        Bukkit.broadcastMessage(ChatColor.GOLD + "[Manhunt] " + ChatColor.RED
                + "Весь процесс подготовки и настройки игры полностью сброшены администратором.");
    }

    private void endGameCleanup(CommandSender sender) {
        cancelCountdown();
        World overworld = getMainOverworld();
        Location defaultSpawn = overworld.getSpawnLocation();

        resetWorldBorder(overworld);

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
            p.setHealth(0.0); // По ТЗ: все умирают, переводятся в выживание и телепортируются на спавн
            p.teleport(defaultSpawn);
        }

        speedrunnerUuid = null;
        hunterUuids.clear();
        readyPlayers.clear();
        selectedDifficulty = null;
        chosenStartLocation = null;
        lastRunnerLocations.clear();
        trackedRunnerEffects.clear();
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

        int highestY = overworld.getHighestBlockYAt(0, 0);
        Location centerLoc = new Location(overworld, 0.5, highestY + 10.0, 0.5);
        if (!speedrunner.getWorld().equals(overworld)
                || Math.abs(speedrunner.getLocation().getX()) > selectedDifficulty.getPickPlaceBorderSize() / 2.0
                || Math.abs(speedrunner.getLocation().getZ()) > selectedDifficulty.getPickPlaceBorderSize() / 2.0) {
            speedrunner.teleport(centerLoc);
        }

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

        double exactX = runnerLoc.getX();
        double exactZ = runnerLoc.getZ();
        int blockX = runnerLoc.getBlockX();
        int blockZ = runnerLoc.getBlockZ();
        int topY = world.getHighestBlockYAt(blockX, blockZ) + 1;

        chosenStartLocation = new Location(world, exactX, topY, exactZ, runnerLoc.getYaw(), runnerLoc.getPitch());
        world.setSpawnLocation(blockX, topY, blockZ);

        // 1 & 2. Телепортация всех игроков на верхний блок по X и Z спидранера и установка спавнпоинта
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(chosenStartLocation);
            if (p.getBedSpawnLocation() == null) {
                p.setBedSpawnLocation(chosenStartLocation, true);
            }
            if (!p.getUniqueId().equals(speedrunnerUuid)) {
                p.setGameMode(GameMode.SURVIVAL);
                setPlayerMaxHealth(p, 20.0);
                p.setHealth(20.0);
                p.setFoodLevel(20);
            }
        }

        // 3. Спидранер получает режим выживания и полное здоровье согласно выбранной сложности
        speedrunner.setGameMode(GameMode.SURVIVAL);
        double maxHp = selectedDifficulty.getSpeedrunnerMaxHealth();
        setPlayerMaxHealth(speedrunner, maxHp);
        speedrunner.setHealth(maxHp);
        speedrunner.setFoodLevel(20);
        speedrunner.setSaturation(20f);

        // 4. Центр мира прикрепляется к этой координате и становится размером 30 на 30 блоков до начала игры
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
            border.setSize(selectedDifficulty.getGameBorderSize());
            border.setSize(1.0, IMPOSSIBLE_SHRINK_SECONDS);
            Bukkit.broadcastMessage(ChatColor.DARK_RED + "[Manhunt] Режим IMPOSSIBLE: граница мира сужается до 1 блока за 4 часа!");
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
                // Скорость V (+60 секунд за каждого охотника)
                if (speedType != null) {
                    effectsToAdd.add(new PotionEffect(speedType, 20 * 60 * hunterCount, 4, false, true, true));
                }
                // Сила II (+10 минут за каждого охотника)
                if (strengthType != null) {
                    effectsToAdd.add(new PotionEffect(strengthType, 20 * 600 * hunterCount, 1, false, true, true));
                }
                // Регенерация II (+15 минут за каждого охотника)
                if (regenType != null) {
                    effectsToAdd.add(new PotionEffect(regenType, 20 * 900 * hunterCount, 1, false, true, true));
                }
                break;

            case MEDIUM:
                // Скорость V (+30 секунд за каждого охотника)
                if (speedType != null) {
                    effectsToAdd.add(new PotionEffect(speedType, 20 * 30 * hunterCount, 4, false, true, true));
                }
                // Сила I (+5 минут за каждого охотника)
                if (strengthType != null) {
                    effectsToAdd.add(new PotionEffect(strengthType, 20 * 300 * hunterCount, 0, false, true, true));
                }
                // Регенерация II (+5 минут за каждого охотника)
                if (regenType != null) {
                    effectsToAdd.add(new PotionEffect(regenType, 20 * 300 * hunterCount, 1, false, true, true));
                }
                break;

            case HARD:
                // Скорость V (+15 секунд за каждого охотника)
                if (speedType != null) {
                    effectsToAdd.add(new PotionEffect(speedType, 20 * 15 * hunterCount, 4, false, true, true));
                }
                break;

            case IMPOSSIBLE:
                // Никаких бустов
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
    // СОБЫТИЯ ИГРЫ (LISTENERS)
    // =========================================================================

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
    public void onRunnerMove(PlayerMoveEvent event) {
        if (gameState != GameState.IN_GAME) return;
        Player player = event.getPlayer();
        if (speedrunnerUuid != null && speedrunnerUuid.equals(player.getUniqueId())) {
            Location to = event.getTo();
            if (to != null && to.getWorld() != null) {
                lastRunnerLocations.put(to.getWorld().getEnvironment(), to.clone());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionEffectChange(EntityPotionEffectEvent event) {
        if (gameState != GameState.IN_GAME) return;
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

        // Компас охотника остаётся при смерти (не выпадает)
        if (hunterUuids.contains(deceased.getUniqueId())) {
            event.getDrops().removeIf(this::isHunterCompass);
        }

        // Смерть спидранера в игре -> победа охотников
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
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCompassDrop(PlayerDropItemEvent event) {
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
        // Достижение «Освободите Энд» (minecraft:end/kill_dragon)
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
        gameState = GameState.ENDED;

        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");
        Bukkit.broadcastMessage(ChatColor.BLUE + "Спидранер погиб! Победа Охотников (Hunters Wins!!)!");
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
        gameState = GameState.ENDED;

        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================================");
        Bukkit.broadcastMessage(ChatColor.RED + "Спидранер победил Эндер-дракона и получил достижение «Освободите Энд»!");
        Bukkit.broadcastMessage(ChatColor.GREEN + "Победа Спидранера!");
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

    private void giveHunterCompass(Player hunter) {
        for (ItemStack item : hunter.getInventory().getContents()) {
            if (isHunterCompass(item)) {
                return;
            }
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
        resetAllPlayerStates();

        speedrunnerUuid = null;
        hunterUuids.clear();
        readyPlayers.clear();
        selectedDifficulty = null;
        chosenStartLocation = null;
        lastRunnerLocations.clear();
        trackedRunnerEffects.clear();

        if (resetWorldBorders) {
            resetWorldBorder(getMainOverworld());
        }
    }

    private void resetWorldBorder(World world) {
        if (world == null) return;
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0.0, 0.0);
        border.setSize(60000000.0);
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
        sender.sendMessage(ChatColor.GRAY + " • Граница мира сужается до 1 блока за 4 часа.");
        sender.sendMessage(ChatColor.GOLD + "===============================================");
    }

    private void sendManhuntHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Команды Asonsss_manhunt (v26.3) ===");
        sender.sendMessage(ChatColor.YELLOW + "/manhunt game start_prepare [new_world] " + ChatColor.GRAY + "— начать подготовку к игре");
        sender.sendMessage(ChatColor.YELLOW + "/manhunt speedruner add/remove <Игрок> " + ChatColor.GRAY + "— назначить/убрать спидранера");
        sender.sendMessage(ChatColor.YELLOW + "/manhunt hunter add/remove <Игрок> " + ChatColor.GRAY + "— назначить/убрать охотника");
        sender.sendMessage(ChatColor.YELLOW + "/manhunt difficulty info " + ChatColor.GRAY + "— информация о сложностях");
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
                return filterStartsWith(Arrays.asList("game", "speedruner", "hunter", "difficulty"), args[0]);
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
                    return null; // Стандартный список онлайн-игроков
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

        return Collections.emptyList();
    }

    private List<String> filterStartsWith(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(opt -> opt.toLowerCase(Locale.ROOT).startsWith(lower))
                .collect(Collectors.toList());
    }
}