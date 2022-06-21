package pacman;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;



public class Model extends JPanel implements ActionListener {
    private Dimension d;
    private  final Font smallFont = new Font("Arial", Font.BOLD,15);
    // Flagi sprawdzajace czy gra w toku i czy pacman zyje
    private boolean inProgress = false;
    private boolean dead = false;

    // Wymiary planszy i stałe w grze
    public final int MINx = 4;
    public final int MAXx = 64;
    public final int MINy = 4;
    public final int MAXy = 32;
    private final int MAX_GHOST = 30;
    private final int MIN_GHOST = 1;
    private final int CUBE_SIZE = 24;
    private int N_OF_CUBES_X;
    private int N_OF_CUBES_Y;
    private int STARTING_SIZE_X;
    private int STARTING_SIZE_Y;
    private int STARTING_GHOSTS_NUM;
    private int MAP_SIZE_X;
    private int MAP_SIZE_Y;
    private int maxBlockWidth;
    private int maxBlockHeight;
    private final int PACMAN_SPEED = 4;
    private double DISTANCE_TO_CHASE;
    private int N_OF_GHOST;
    // zmienne takie jak predkosci, polozenie,kierunki itd
    private int lives, score = 0, pointsOnMap;
    private final int START_LIVES = 5;
    private int [] dx,dy;
    private int [] ghostX,ghostY,ghostDX,ghostDY,ghostSpeed;
    private Image live, ghost;
    private Image up, down, left,right;
    private int pacmanX,pacmanY,pacmanDX,pacmanDY,pacmanCubeX,pacmanCubeY,ghostCubeX,ghostCubeY;
    private int dirDX,dirDY;
    private final int validSpeed[] = {1,4,2,3,3,3};
    private final int maxSpeed = 5;
    private int currentSpeed = 3;
    private final int CURRENT_SPEED_START = 3;
    private short [] fieldValue;
    private Timer measureTime;
    private long startTime;
    private final int IMMORALITY_TIME = 200; // czas początkowej nieśmiertelności w ms
    // ułożenie podstawowej mapy, wzór:
    // 0 dla przeszkody, 16 wolne pole, +1 od lewej, +2 od góry, +4 od prawej, +8 od dolu
    private final short Map1[]={
            19, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
            17, 16, 16, 16, 16, 24, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 28, 0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
            0,  0,  0,  0,  0,  0, 17, 16, 16, 24, 24, 24, 24, 24, 20,
            19, 18, 18, 18, 18, 18, 16, 16, 20, 0, 0,  0,  0,   0, 21,
            17, 16, 16, 16, 16, 16, 16, 16, 20, 0, 0,  0,  0,   0, 21,
            17, 16, 16, 16, 16, 16, 16, 16, 20, 0, 0,  0,  0,   0, 21,
            17, 16, 16, 16, 24, 24, 24, 24, 20, 0, 0,  0,  0,   0, 21,
            17, 16, 16, 20, 0,  0,  0,   0, 17, 18, 18, 18, 18, 18, 20,
            17, 24, 24, 28, 0,  0,  0,   0, 17, 16, 16, 16, 16, 16, 20,
            21, 0,  0,  0,  0,  0,  0,   0, 17, 16, 16, 16, 16, 16, 20,
            17, 18, 18, 22, 0, 19, 18, 18, 16, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 26, 24, 24, 24, 24, 24, 24, 24, 24, 24, 28

    };
    private short Map[] = new short[(MAXx+1)*(MAXy+1)];

    //////////////////////////////////////////////////////////////////////////////////////
    // Wywoływawnie funkcji i inicjowanie zmienncyh
    ////////////////////////////////////////////////////////////////////////////////////////
    // konstruktor zawierajacy inicjalizacje poczatku gry
    public Model(int xDim, int yDim, int gNum)
    {
        this.N_OF_CUBES_X = xDim;
        this.N_OF_CUBES_Y = yDim;
        this.N_OF_GHOST = gNum;
        this.STARTING_SIZE_X = xDim;
        this.STARTING_SIZE_Y = yDim;
        this.STARTING_GHOSTS_NUM = gNum;
        this.MAP_SIZE_X = N_OF_CUBES_X*CUBE_SIZE;
        this.MAP_SIZE_Y = N_OF_CUBES_Y*CUBE_SIZE;
        this.maxBlockWidth = (int) N_OF_CUBES_X/MINx;
        this.maxBlockHeight = (int) N_OF_CUBES_Y/MAXy;
        this.DISTANCE_TO_CHASE = (xDim + yDim)/5.3;
        System.out.println("distance to chase: " + DISTANCE_TO_CHASE);
        generateMap();
        generateObjects();
        initVariables();
        addKeyListener(new TAdapter());
        setFocusable(true);
        startGame(START_LIVES);
    }

    // generowanie mapy
    private void generateMap() {
        translateMap(generateBinMap());
    }

    // tłumaczenie mapy binarnej - zawierającej tylko informacje o przeszkodach,
    // na mapę docelową wg. wzoru:
    // 0 dla przeszkody, 16 wolne pole, +1 od lewej, +2 od góry, +4 od prawej, +8 od dolu
    private void translateMap(short[] binMap)
    {
        for (int i = 0; i < N_OF_CUBES_X*N_OF_CUBES_Y; i++)
        {
            if (binMap[i] == 0) Map[i] = 0; // przeszkoda
            else // wolne pole
            {
                Map[i] = 16;

                // krawędź lub przeszkoda po lewej
                if (i > 0) {
                    if (i % N_OF_CUBES_X == 0 || binMap[i-1] == 0) Map[i] += 1;
                } else {
                    Map[i] += 1;
                }

                // krawędź lub przeszkoda po prawej
                if (i < N_OF_CUBES_X*N_OF_CUBES_Y - 1) {
                    if (i % N_OF_CUBES_X == N_OF_CUBES_X - 1 || binMap[i+1] == 0) Map[i] += 4;
                } else {
                    Map[i] += 4;
                }

                // krawędź lub przeszkoda u góry
                if(i >= N_OF_CUBES_X) {
                    if (binMap[i - N_OF_CUBES_X] == 0) Map[i] += 2;
                } else {
                    Map[i] += 2;
                }

                // krawędź lub przeszkoda u dołu
                if(i <= N_OF_CUBES_X*(N_OF_CUBES_Y - 1) -  1) {
                    if (binMap[i + N_OF_CUBES_X] == 0) Map[i] += 8;
                } else {
                    Map[i] += 8;
                }
            }
        }
    }

    // funkcja generująca mapę w postaci 0 i 1 (0 odpowiada przeszkodzie, a 1 wolnemu polu)
    // początkowo ustawia 0, czyli przeszkodę na każdym polu, później wywołuje funkcję divide
    // odpowiedzialną za naniesienie ścieżek
    private short[] generateBinMap()
    {
        short binMap[] = new short[N_OF_CUBES_X*N_OF_CUBES_Y];
        for (int i = 0; i < N_OF_CUBES_X*N_OF_CUBES_Y; i++)
        {
            binMap[i] = 0;
        }
        divide(0, N_OF_CUBES_X-1, 0, N_OF_CUBES_Y-1, binMap, true);

        for (int i = 0; i < N_OF_CUBES_X*N_OF_CUBES_Y; i++)
        {
            if (i % N_OF_CUBES_X == 0 || i % N_OF_CUBES_X == N_OF_CUBES_X - 1
                    || i < N_OF_CUBES_X || i > N_OF_CUBES_X*(N_OF_CUBES_Y-1))
            {
                binMap[i] = 1; // wolne pola przy krawędziach
            }
        }
        return binMap;
    }

    // Fukcja rysująca ścieżki
    // olega na losowym wybieraniu indeksu podziału i generowaniu w jego miejscu ścieżki,
    // raz pionowo, raz poziomo, poprzez wywoływanie rekurencyjne funkcji divide() dla
    // dażdej podzielonej częsci
    private void divide(int xStart, int xEnd, int yStart, int yEnd, short binMap[], boolean bool)
    {
        int width = xEnd - xStart + 1;
        int height = yEnd - yStart + 1;
        int random = (int)(Math.random()*(10000));
        int randX;
        int randY;
        if (bool) {
            if(width > 3) {
                randX = random % (width - 1);
                for (int i = 0; i < height; i++)
                {
                    binMap[(yStart + i) * N_OF_CUBES_X + xStart + randX] = 1;
                }
                if (randX - 1 >= 0) divide(xStart, xStart + randX - 1, yStart, yEnd, binMap, !bool);
                if (randX + 1 <= xEnd - xStart) divide(xStart + randX + 1, xEnd, yStart, yEnd, binMap, !bool);

            } else if (height > 3) {
                randY = random % (height - 1);
                for (int i = 0; i < width; i++)
                {
                    binMap[(yStart + randY)*N_OF_CUBES_X + xStart + i] = 1;
                }
                if (randY - 1 >= 0) divide(xStart, xEnd, yStart, yStart + randY - 1, binMap, !bool);
                if (randY + 1 <= yEnd - yStart) divide(xStart, xEnd, yStart + randY + 1, yEnd, binMap, !bool);
            } else {
                return;
            }
        } else {
            if (height > 3) {
                randY = random % (height - 1);
                for (int i = 0; i < width; i++) {
                    binMap[(yStart + randY) * N_OF_CUBES_X + xStart + i] = 1;
                }
                if (randY - 1 >= 0) divide(xStart, xEnd, yStart, yStart + randY - 1, binMap, !bool);
                if (randY + 1 <= yEnd - yStart) divide(xStart, xEnd, yStart + randY + 1, yEnd, binMap, !bool);
            } else if (width > 3) {
                randX = random % (width - 1);
                for (int i = 0; i < height; i++)
                {
                    binMap[(yStart + i) * N_OF_CUBES_X + xStart + randX] = 1;
                }
                if (randX - 1 >= 0) divide(xStart, xStart + randX - 1, yStart, yEnd, binMap, !bool);
                if (randX + 1 <= xEnd - xStart) divide(xStart + randX + 1, xEnd, yStart, yEnd, binMap, !bool);
            } else {
                return;
            }
        }
    }

    // dodanie gifow i jpegow
    private void generateObjects()
    {
        down =new ImageIcon("src/images/down.gif").getImage();
        up =new ImageIcon("src/images/up.gif").getImage();
        left =new ImageIcon("src/images/left.gif").getImage();
        right =new ImageIcon("src/images/right.gif").getImage();
        ghost =new ImageIcon("src/images/ghost.gif").getImage();
        live =new ImageIcon("src/images/heart.png").getImage();

    }

    private void initVariables(){
        fieldValue = new short[N_OF_CUBES_X*N_OF_CUBES_Y];
        if(N_OF_CUBES_X < 12) d = new Dimension(CUBE_SIZE*12,CUBE_SIZE*N_OF_CUBES_Y + 30);
        else d = new Dimension(CUBE_SIZE*N_OF_CUBES_X,CUBE_SIZE*N_OF_CUBES_Y + 30);
        ghostX = new int [MAX_GHOST];
        ghostDX = new int [MAX_GHOST];
        ghostDY = new int [MAX_GHOST];
        ghostY = new int [MAX_GHOST];
        ghostSpeed = new int [MAX_GHOST];
        dy = new int[4];
        dx = new int[4];
        // Predkosc gry w ms
        measureTime = new Timer(60,this);
        measureTime.start();

    }

    // parametry pocztkowee
    private void startGame(int l)
    {
        dead = false;
        lives = l;
        initMap();
    }

    // funkcja wywołująca  stworki i ich ruchy
    private void playGame(Graphics2D graph2d) throws InterruptedException {
        if (dead)
        {
            gameOver();
        }
        else{
            movePacman();
            showPacman(graph2d);

            // wątki duszków
            Runnable[] ghosts = new Runnable[N_OF_GHOST];
            Thread[] ghostThreads = new Thread[N_OF_GHOST];

            for(int i=0; i<N_OF_GHOST; i++) {
                ghosts[i] = new GhostRun(i, graph2d);
            }

            for(int i=0; i<N_OF_GHOST; i++) {
                ghostThreads[i] = new Thread(ghosts[i]);
            }

            for(int i=0; i<N_OF_GHOST; i++) {
                ghostThreads[i].start();
            }

            for(int i=0; i<N_OF_GHOST; i++) {
                ghostThreads[i].join();
            }

            checkMap();
        }
    }

    // wczytywanie mapki
    private void initMap()
    {
        int i;
        for (i=0;i<N_OF_CUBES_X*N_OF_CUBES_Y;i++)
        {
            // przypisywanie wszystkich zmiennych z mapki
            fieldValue[i] = Map[i];
        }
        dead = false;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Sterowanie i logika
    /////////////////////////////////////////////////////////////////////////////////////////////////
    // PACMAN
    // logika odpowiedzialna za ruch pacmana i zdobywanie punktów
    public void movePacman()
    {
        int pos,ch;
        // na poczatku pozycja Pacmana jest zdeterminowana
        if(pacmanX % CUBE_SIZE == 0 && pacmanY %CUBE_SIZE==0)
        {
            pos = pacmanX/CUBE_SIZE+N_OF_CUBES_X*(int)(pacmanY/CUBE_SIZE);
            ch=fieldValue[pos];
            // Gdy pacman znajduje sie na polu gdzie jest jedzenie 16,17,19....
            if((ch&16)!=0)
            {
                // zmienia wartosc pozycji na taka, która bedzie mniejsza od 16 ale
                // ciagle bedzie okreslala czy znajdujesz sie obok krwedzi oraz dodaje punkt
                fieldValue[pos]=(short)(ch & 15);
                score++;
            }
            // jezeli pacmanowi zostanie nadany nowy kierunek to sprawdzamy czy obok nie ma sciany
            // robimy to za pomoca symbolu & ktory determinuje przeszkode w danym miejscu na planszy
            // jezeli nie wykryje przeszkody moze poruszac sie dalej w tym kierunku
            if(dirDX != 0|| dirDY != 0)
            {
                if(!((dirDX==-1 && dirDY ==0 &&(ch & 1)!=0)||
                        (dirDX==1 && dirDY ==0 &&(ch & 4)!=0)||
                        (dirDX==0 && dirDY ==-1 &&(ch & 2)!=0)||
                        (dirDX==0 && dirDY ==1 &&(ch & 8)!=0)))
                {
                    pacmanDX=dirDX;
                    pacmanDY=dirDY;
                }
            }
            // to samo co wyzej tylko gdy pacman porusza sie w danym kierunku
            if((pacmanDX == -1 && pacmanDY==0 && (ch & 1) !=0)
                    ||(pacmanDX == 1 && pacmanDY==0 && (ch & 4) !=0)
                    ||(pacmanDX == 0 && pacmanDY==-1 && (ch & 2) !=0)
                    ||(pacmanDX == 0 && pacmanDY==1 && (ch & 8) !=0))
            {
                pacmanDX=0;
                pacmanDY=0;
            }
        }
        // poruszanie sie pacmana
        pacmanY=pacmanY + PACMAN_SPEED*pacmanDY;
        pacmanX=pacmanX + PACMAN_SPEED*pacmanDX;

    }

    // funkcja rysuje pacmana w odpowiedzniej pozycji
    public void showPacman(Graphics2D graph2d)
    {
        if(dirDX==-1){
            graph2d.drawImage(left,pacmanX+1,pacmanY+1,this);
        }
        else if(dirDX==1){
            graph2d.drawImage(right,pacmanX+1,pacmanY+1,this);
        }
        else if(dirDY==-1){
            graph2d.drawImage(up,pacmanX+1,pacmanY+1,this);
        }
        else {
            graph2d.drawImage(down,pacmanX+1,pacmanY+1,this);
        }
    }

    // Obsługa klawiszy sterowanie wsad, enter pauza, spacja nowa gra
    class TAdapter extends KeyAdapter{
        @Override
        public void keyPressed(KeyEvent e){
            int key = e.getKeyCode();
            if(inProgress){
                if (key == KeyEvent.VK_A){
                    dirDX = -1;
                    dirDY = 0;

                }
                else if (key == KeyEvent.VK_D){
                    dirDX = 1;
                    dirDY = 0;

                }
                else if (key == KeyEvent.VK_W){
                    dirDX = 0;
                    dirDY = -1;

                }
                else if (key == KeyEvent.VK_S){
                    dirDX = 0;
                    dirDY = 1;

                }
                else if (key == KeyEvent.VK_P && measureTime.isRunning()) {
                    inProgress = false;
                }
                else if (key == KeyEvent.VK_ENTER && measureTime.isRunning()){
                    inProgress = false;
                    N_OF_CUBES_X = STARTING_SIZE_X;
                    N_OF_CUBES_Y = STARTING_SIZE_Y;
                    N_OF_GHOST = STARTING_GHOSTS_NUM;
                    currentSpeed = CURRENT_SPEED_START;
                    score = 0;
                    gameOver();
                }

            }
            else
            {
                if (key == KeyEvent.VK_SPACE)
                {
                    startTime = System.currentTimeMillis();
                    inProgress=true;
                    startGame(START_LIVES);
                }

                if (key == KeyEvent.VK_P)
                {
                    inProgress=true;
                    startGame(lives);
                }
            }
            // code to lvl Up = shift + tilde
            if (key == KeyEvent.VK_DEAD_TILDE)
            {
                pointsOnMap = 0;
                for (int i = 0; i < N_OF_CUBES_X*N_OF_CUBES_Y; i++)
                {
                    if((fieldValue[i])!=0 && (fieldValue[i] & 16) == 16)
                    {
                        pointsOnMap++;
                    }
                }
                score += pointsOnMap;
                lvlUp();
            }
        }
    }

    // DUSZKI
    // ruch dla duszków
    public class GhostRun implements Runnable {

        private int id;
        Graphics2D graph2d;

        private void drawMove(int count, int id)
        {
            // losowanie ruchu spośród wszystkich możliwych
            int choose = (int)(Math.random()*count); // losowanie liczby od 0 do count
            if (choose > 3)
            {
                choose = 3;
            }
            ghostDX[id]=dx[choose]; // zmiana ruchu
            ghostDY[id]=dy[choose];
        }
        public GhostRun(int id, Graphics2D g) {
            this.id = id;
            Graphics2D graph2d = g;
            this.graph2d = graph2d;
        }

        @Override
        public void run() {
            int pos,count;
            int i = id;
            double distance;
            boolean left, right, up, down;

            // Kontrola ruchu duszków
            // Jest wywoływana kiedy duch nie znajduje się pomiędzy polami, co sprawdza poniższy warunek
            if(ghostX[i] % CUBE_SIZE == 0 && ghostY[i] % CUBE_SIZE==0)
            {
                pos = ghostX[i]/CUBE_SIZE + N_OF_CUBES_X *(int)(ghostY[i]/CUBE_SIZE); // pozycja ducha (Y*maxX + X)
                count = 0;
                left = right = up = down = false;

                // brak przeszkody po lewej, ruch w pionie
                if((fieldValue[pos] & 1)==0 && ghostDX[i] != 1)
                {
                    dx[count]=-1; // możliwy ruch w lewo
                    dy[count]=0;  // zatrzymanie ruchu w pionie
                    left = true;
                    count++;
                }
                // brak przeszkody u góry, ruch w poziomie
                if((fieldValue[pos] & 2)==0 && ghostDY[i] != 1)
                {
                    dx[count]=0;  // zatrzymanie ruchu w poziomie
                    dy[count]=-1; // możliwy ruch w górę
                    up = true;
                    count++;
                }
                // brak przeszkody po prawej, ruch w pionie
                if((fieldValue[pos] & 4)==0 && ghostDX[i] != -1)
                {
                    dx[count]=1; // możliwy ruch w prawo
                    dy[count]=0; // zatrzymanie ruchu w pionie
                    right = true;
                    count++;
                }
                // brak przeszkody u dołu, ruch w poziomie
                if((fieldValue[pos] & 8)==0 && ghostDY[i] != -1)
                {
                    dx[count]=0; // zatrzymanie ruchu w poziomie
                    dy[count]=1; // możliwy ruch na dół
                    down = true;
                    count++;
                }
                if(count == 0) // jeśli żaden ruch nie jest możliwy
                {
                    if((fieldValue[pos]&15)==15) // jeśli jest na wolnym polu
                    {
                        ghostDX[i]=0; // zatrzymanie ruchu
                        ghostDY[i]=0;
                    }
                    else // jeśli jest na zajętym polu
                    {
                        ghostDY[i] = -ghostDY[i]; // odwrócenie ruchu
                        ghostDX[i] = -ghostDX[i];

                    }
                }
                else // jeśli jest możliwy ruch
                {
                    // jeśli znajdujemy się w pobliżu pacmana zaczynamy go gonić
                    distance = Math.sqrt(Math.pow((ghostX[i]-pacmanX),2) + Math.pow((ghostY[i]-pacmanY),2))/CUBE_SIZE;
                    if (distance <= DISTANCE_TO_CHASE)
                    {
                        System.out.println(distance);
                        if(pacmanX - ghostX[i] <= 0) //pacman po lewej
                        {
                            if (left)
                            {
                                ghostDX[i] = -1;
                                ghostDY[i] = 0;
                            }
                            else if (pacmanY - ghostY[i] <= 0) //pacman u góry
                            {
                                if (up)
                                {
                                    ghostDX[i] = 0;
                                    ghostDY[i] = -1;
                                }
                                else drawMove(count, i);
                            }
                            else // pacman na dole
                            {
                                if (down)
                                {
                                    ghostDX[i] = 0;
                                    ghostDY[i] = 1;
                                }
                                else drawMove(count, i);
                            }
                        }
                        else // pacman po prawej
                        {
                            if (right)
                            {
                                ghostDX[i] = 1;
                                ghostDY[i] = 0;
                            }
                            else if (pacmanY - ghostY[i] <= 0) //pacman u góry
                            {
                                if (up)
                                {
                                    ghostDX[i] = 0;
                                    ghostDY[i] = -1;
                                }
                                else drawMove(count, i);
                            }
                            else // pacman na dole
                            {
                                if (down)
                                {
                                    ghostDX[i] = 0;
                                    ghostDY[i] = 1;
                                }
                                else drawMove(count, i);
                            }
                        }
                    }
                    else drawMove(count, i);
                }
            }
            ghostX[i]=ghostX[i]+(ghostDX[i]*ghostSpeed[i]);
            ghostY[i]=ghostY[i]+(ghostDY[i]*ghostSpeed[i]);
            drawGhost(graph2d, ghostX[i]+1,ghostY[i]+1);

            //jesli duszek dotknie pacmana
            if (pacmanX>(ghostX[i]-12)&&pacmanX<(ghostX[i]+12)
                    &&(pacmanY>(ghostY[i]-12)&&pacmanY<(ghostY[i]+12)) && inProgress)
            {
                dead=true;
            }
        }
    }

    // tworzymy duchy i pacmana oraz zadajemy ich poczatkowa predkosc i polozenie
    private void initCreatures()
    {
        int dx = 1;
        int random;
        int i;

        for(i = 0; i < N_OF_GHOST; i++)
        {
            ghostCubeX = (int) N_OF_CUBES_X/2;
            ghostCubeY = (int) N_OF_CUBES_Y/2;
            while (Map[ghostCubeY*N_OF_CUBES_X + ghostCubeX] == 0) {
                if (ghostCubeX > 0) ghostCubeX--;
                else {
                    ghostCubeX = N_OF_CUBES_X;
                    ghostCubeY--;
                }
            }
            ghostX[i] = ghostCubeX*CUBE_SIZE;
            ghostY[i] = ghostCubeY*CUBE_SIZE;
            ghostDY[i] = 0;
            ghostDX[i] = dx;
            dx = -dx;
        }

        if (lives == START_LIVES)
        {
            for(i = 0; i < N_OF_GHOST; i++)
            {
                random = (int)(Math.random()*(currentSpeed+1));
                if(random>currentSpeed){
                    random = currentSpeed;
                }
                ghostSpeed[i] = validSpeed[random];
            }
        }

        pacmanCubeX = 0;
        pacmanCubeY = 0;
        while (Map[pacmanCubeY*N_OF_CUBES_X + pacmanCubeX] == 0) {
            if (pacmanCubeX < N_OF_CUBES_X) pacmanCubeX++;
            else {
                pacmanCubeX = 0;
                pacmanCubeY++;
            }
        }

        pacmanX = pacmanCubeX*CUBE_SIZE;
        pacmanY = pacmanCubeY*CUBE_SIZE;
        pacmanDX = 0;
        pacmanDY = 0;
        dirDX = 0;
        dirDY=0;
        dead=false;
    }

    // MAPKA
    // funkcja odpowiedzialna za zbieranie punktow na mapce
    private void checkMap()
    {
        int i=0;
        boolean finished = true;
        // jesli na zadnym polu nie zostanie kropka to przejdz do kolejnego poziomu
        while(i<N_OF_CUBES_X*N_OF_CUBES_Y && finished)
        {

            if((fieldValue[i])!=0 && (fieldValue[i] & 16) == 16)
            {
                finished=false;
            }
            i++;
        }
        if (finished) lvlUp();
    }

    private void lvlUp()
    {
        // zwieksz ilosc duchow i ich predkosc
        score+=50;
        if(N_OF_GHOST<MAX_GHOST){
            N_OF_GHOST++;
        }

        if(currentSpeed<maxSpeed)
        {
            //currentSpeed++;
        }
            /*
            // zwiększ rozmiar mapy
            if (N_OF_CUBES_X < MAXx) N_OF_CUBES_X++;
            if (N_OF_CUBES_Y < MAXy) N_OF_CUBES_Y++;
            this.MAP_SIZE_X = N_OF_CUBES_X*CUBE_SIZE;
            this.MAP_SIZE_Y = N_OF_CUBES_Y*CUBE_SIZE;
            this.maxBlockWidth = (int) N_OF_CUBES_X/MINx;
            this.maxBlockHeight = (int) N_OF_CUBES_Y/MAXy;
            this.DISTANCE_TO_CHASE = (N_OF_CUBES_X + N_OF_CUBES_Y)/5.3;
            System.out.println("distance to chase: " + DISTANCE_TO_CHASE);
             */
        gameOver();
        generateMap();
        generateObjects();
        initVariables();
        startGame(START_LIVES);
        inProgress = false;
    }

    private void gameOver()
    {
        // jezeli brak zyc to koniec gry
        if(lives == 0) {
            inProgress=false;
            N_OF_CUBES_X = STARTING_SIZE_X;
            N_OF_CUBES_Y = STARTING_SIZE_Y;
            N_OF_GHOST = STARTING_GHOSTS_NUM;
            currentSpeed = CURRENT_SPEED_START;
            score = 0;
        }
        initCreatures();
        if (System.currentTimeMillis() - startTime > IMMORALITY_TIME) {
            lives--;
            // po dotknieciu z duszkiem pacman traci jedno zycie
            System.out.println("Death");
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // GRAFIKA
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // wiadomośc poczatkowa
    public void introScreen(Graphics2D graph2d)
    {
        String message = "Hello, press SPACE if you want to play";
        String message1 = "<-AWSD->, ENTER = retry, P = pause";
        graph2d.setColor(Color.CYAN);
        if(N_OF_CUBES_X < 12) {
            graph2d.drawString(message, 4,MAP_SIZE_Y/2-20);
            graph2d.drawString(message1, 12,MAP_SIZE_Y/2+20);
        }
        else {
            graph2d.drawString(message, MAP_SIZE_X/2 - 140,MAP_SIZE_Y/2-20);
            graph2d.drawString(message1, MAP_SIZE_X/2 - 140,MAP_SIZE_Y/2+20);
        }
    }

    public void drawScore(Graphics2D graph2d)
    {
        graph2d.setFont(smallFont);
        graph2d.setColor(new Color(5,151,79));
        String s = "Your score: "+score;
        if (N_OF_CUBES_X < 12) graph2d.drawString(s,160,MAP_SIZE_Y+16);
        else graph2d.drawString(s,N_OF_CUBES_X*CUBE_SIZE - 130,MAP_SIZE_Y+16);
        for (int i = 0;i<lives;i++)
        {
            graph2d.drawImage(live,i*28+8,MAP_SIZE_Y+1,this);
        }

    }

    public void drawGhost(Graphics2D graph2d,int x, int y )
    {
        graph2d.drawImage(ghost,x,y,this);
    }

    // po zebraniu wszystkich punktów
    public void drawMap(Graphics2D graph2d)
    {
        short i = 0;
        int x,y;
        for(y=0;y<MAP_SIZE_Y; y+=CUBE_SIZE)
        {
            for(x=0;x<MAP_SIZE_X; x+=CUBE_SIZE)
            {
                //graph2d.setColor(new Color(220,20,0));
                graph2d.setColor(new Color(160,50,50));
                graph2d.setStroke(new BasicStroke(5));
                // rysowanie przeszkod i ramki
                if((Map[i]==0))
                {
                    graph2d.fillRect(x,y,CUBE_SIZE,CUBE_SIZE);
                }
                if((fieldValue[i]&1)!=0)
                {
                    graph2d.drawLine(x,y,x,y+CUBE_SIZE-1);
                }
                if((fieldValue[i]&2)!=0)
                {
                    graph2d.drawLine(x,y,x+CUBE_SIZE-1,y);
                }
                if((fieldValue[i]&4)!=0)
                {
                    graph2d.drawLine(x+CUBE_SIZE-1,y,x+CUBE_SIZE-1,y+CUBE_SIZE-1);
                }
                if((fieldValue[i]&8)!=0)
                {
                    graph2d.drawLine(x,y+CUBE_SIZE-1,x+CUBE_SIZE-1,y+CUBE_SIZE-1);
                }
                // kropki
                if((fieldValue[i]&16)!=0)
                {
                    //graph2d.setColor(new Color(255,255,255));
                    graph2d.setColor(new Color(190,190,200));
                    graph2d.fillOval(x+10,y+10,6,6);
                }
                i++;
            }
        }
    }

    // nadawanie kolorów mapie
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D graph2d = (Graphics2D) g;
        //graph2d.setColor(new Color(0,50,255));
        graph2d.setColor(new Color(10,30,30));
        graph2d.fillRect(0,0,d.width,d.height);
        drawMap(graph2d);
        drawScore(graph2d);
        if (inProgress) {
            try {
                playGame(graph2d);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        else{
            introScreen(graph2d);
        }
        Toolkit.getDefaultToolkit().sync();
        graph2d.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}
