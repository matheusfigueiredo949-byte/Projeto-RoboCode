package blitiz; 

import robocode.*;
import java.awt.Color;
import robocode.util.Utils;

/**
 * Blitz - Tanque Ofensivo Agressivo (Homenagem ao Blitzcrank).
 * Estratégia: Movimento de Turbo com penalidade, Tiro de Gancho (3.0) em alvos
 * parados/próximos e tentativa de Colisão/Power Fist em curta distância.
 */
public class Blitz extends AdvancedRobot {
    
    // ATRIBUTOS (Encapsulamento)
    private double energiaAlvo = 100.0;
    private double direcaoMovimento = 1; 
    private long turboStartTime = 0; // Para controlar o tempo de Turbo (W)
    private final static long TURBO_DURATION = 100; // Duração do Turbo (em ticks)
    private final static long TURBO_PENALTY = 50; // Duração da penalidade de lentidão

    // --- Configuração e Loop Principal (W: Turbo) ---
    
    public void run() {
        // Cores (Golem de Vapor)
        setBodyColor(new Color(0, 100, 150)); 
        setGunColor(new Color(255, 165, 0)); 
        setRadarColor(new Color(255, 255, 0)); 

        setAdjustGunForRobotTurn(true); 
        setAdjustRadarForGunTurn(true);
        
        while (true) {
            
            // Lógica do W: Turbo (Overdrive)
            long currentTime = getTime();
            
            if (currentTime < turboStartTime + TURBO_DURATION) {
                // Modo TURBO: Alta Velocidade
                setMaxVelocity(8);
                setAhead(300 * direcaoMovimento);
                setTurnRight(Utils.normalRelativeAngle(360 / 5.0));
            
            } else if (currentTime < turboStartTime + TURBO_DURATION + TURBO_PENALTY) {
                // MODO PENALIDADE AJUSTADO: Desaceleração, mas SEM PARAR
                setMaxVelocity(3); 
                setAhead(100 * direcaoMovimento * -1); // Move-se para trás lentamente
                setTurnLeft(90); 
            } else {
                // Modo Padrão: Movimento de Patrulha
                setMaxVelocity(8);
                setAhead(150 * direcaoMovimento);
                setTurnRight(30 * direcaoMovimento);
            }
            
            // R: Campo Estático (Varredura constante)
            if (getRadarTurnRemaining() == 0) {
                setTurnRadarRight(360);
            }
            
            // Passiva: Barreira de Mana (Movimento Evasivo se Vida Baixa)
            if (getEnergy() < 30) {
                setTurnRight(Utils.normalRelativeAngle(90 * Math.random() - 45));
                setAhead(400); 
            }

            execute(); 
        }
    }

    // --- Q: Puxão Biônico (Rocket Grab) ---
    
    public void onScannedRobot(ScannedRobotEvent e) {
        // Lógica de Mira Preditiva
        double anguloAbsoluto = getHeadingRadians() + e.getBearingRadians();
        double anguloGiroArma = Utils.normalRelativeAngle(anguloAbsoluto - getGunHeadingRadians());
        
        // Travar o Radar
        double anguloGiroRadar = Utils.normalRelativeAngle(anguloAbsoluto - getRadarHeadingRadians());
        setTurnRadarRightRadians(anguloGiroRadar * 2);
        setTurnGunRightRadians(anguloGiroArma);

        // --- MOVIMENTO: COLIDIR ou ORBITAR ---
        if (e.getDistance() < 100) {
            // NOVO: Modo COLISÃO / PUNHO DO PODER (E): Ataca diretamente
            setTurnRight(e.getBearing()); 
            setAhead(e.getDistance() + 5); 
        } else {
            // Modo PADRÃO: Orbitagem (Evasão + Ataque lateral)
            setTurnRight(e.getBearing() + 90); 
            setAhead(150 * direcaoMovimento); 
        }
        
        // Q: Puxão Biônico (Disparar o "Gancho" apenas em condição ideal)
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 5) {
            if (e.getDistance() < 200 || Math.abs(e.getVelocity()) < 2) {
                setFire(3.0); // Tiro de alta potência = O "Gancho" que vai acertar
            } else {
                setFire(1.0); // Tiro de poke (Dano do R)
            }
        }
        
        // Ativação do W: Turbo se a energia caiu
        if (getTime() > turboStartTime + TURBO_DURATION + TURBO_PENALTY && e.getEnergy() < energiaAlvo) {
            turboStartTime = getTime();
            direcaoMovimento *= -1; // Inverte direção de orbitagem
        }

        energiaAlvo = e.getEnergy(); 
        execute();
    }

    // --- E: Punho do Poder (Power Fist) ---
    
    public void onHitRobot(HitRobotEvent e) {
        // E: Punho do Poder: Colisão seguida de soco e recuo
        if (e.isMyFault()) {
            setBack(100); // Recua após o "soco" (Push)
        }
        setFire(3.0); // Tiro de dano máximo de curto alcance
        turboStartTime = getTime(); // Ativa o turbo para fugir
        execute();
    }

    // --- Outros Eventos ---
    
    public void onHitWall(HitWallEvent e) {
        direcaoMovimento *= -1;
    }
    
    public void onHitByBullet(HitByBulletEvent e) {
        // Reage à bala com o W: Turbo para fugir e flanquear
        turboStartTime = getTime();
    }
}