package atom; 

import robocode.*;
import java.awt.Color;
import robocode.util.Utils;

/**
 * Atom - Tanque Isca e Defesa (O Robô Sombra/Atraente).
 * Estratégia: Atrair o inimigo para o centro do mapa com vulnerabilidade controlada,
 * usando movimento reativo para sobreviver e evitar travamento na parede.
 */
public class Atom extends AdvancedRobot {
    
    // ATRIBUTOS (Encapsulamento)
    private double energiaAlvo = 100.0;
    // Variável para evitar o travamento na parede e controlar o movimento de atração
    private double direcaoMovimento = 1; 
    
    // Posição alvo para onde o Atom tenta atrair o inimigo (Centro do Mapa 800x600)
    private final static double CENTRO_X = 400; 
    private final static double CENTRO_Y = 300; 

    // --- Configuração e Loop Principal (Movimento de Isca) ---
    
    public void run() {
        // Cores (Aço Oxidado e Luzes Azuis - Homenagem a Gigantes de Aço)
        setBodyColor(new Color(100, 100, 100)); // Cinza Escuro
        setGunColor(new Color(0, 50, 200));    // Azul Escuro/Neon
        setRadarColor(Color.CYAN);             // Ciano

        setAdjustGunForRobotTurn(true); 
        setAdjustRadarForGunTurn(true);
        setMaxVelocity(5); // Velocidade baixa para parecer um alvo fácil
        
        while (true) {
            
            // 1. Movimento de Atração para o Centro
            double anguloParaCentro = Utils.normalAbsoluteAngle(
                Math.atan2(CENTRO_X - getX(), CENTRO_Y - getY())
            );
            
            // Gira o corpo para apontar para o centro
            setTurnRightRadians(Utils.normalRelativeAngle(anguloParaCentro - getHeadingRadians()));
            
            // Vai para frente (ou para trás se direcaoMovimento for -1)
            setAhead(200 * direcaoMovimento); 

            // Varredura constante e lenta
            setTurnRadarRight(360);
            
            execute();
        }
    }

    // --- Ataque de Contato e Dano de Isca (onScannedRobot) ---
    
    public void onScannedRobot(ScannedRobotEvent e) {
        
        // 1. Lógica de Mira (Trava simples)
        double anguloAbsoluto = getHeadingRadians() + e.getBearingRadians();
        double anguloGiroArma = Utils.normalRelativeAngle(anguloAbsoluto - getGunHeadingRadians());
        
        // Trava Radar
        setTurnRadarRightRadians(Utils.normalRelativeAngle(anguloAbsoluto - getRadarHeadingRadians()) * 2);
        setTurnGunRightRadians(anguloGiroArma);
        
        // 2. Tiro de Isca (Poke)
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 5) {
            // Tiro de Gancho/Contato: Se muito perto ou parado
            if (e.getDistance() < 150 || Math.abs(e.getVelocity()) < 1) {
                setFire(3.0); 
            } else {
                setFire(0.5); // Tiro de POKE: Apenas para irritar e chamar a atenção
            }
        }
        
        // 3. Manobra de Isca Evasiva (Reativa)
        // Se a energia do inimigo caiu, inverte a direção para que o tiro dele erre.
        if (e.getEnergy() < energiaAlvo) {
            direcaoMovimento *= -1; // Inverte o movimento para sair da linha de fogo
        }

        energiaAlvo = e.getEnergy(); 
        execute();
    }

    // --- Defesa Reativa (onHitByBullet) ---
    
    /**
     * Reação ao ser atingido: Gira a 90 graus para desviar da próxima bala
     * (Simula o movimento "Sombra" reativo).
     */
    public void onHitByBullet(HitByBulletEvent e) {
        // Gira rapidamente para um lado (desvia do próximo tiro)
        setTurnRight(90); 
        setAhead(100);
        execute();
    }
    
    // --- Batalha de Contato (onHitRobot) ---
    
    /**
     * Força o recuo do inimigo ao colidir.
     */
    public void onHitRobot(HitRobotEvent e) {
        // Empurra o inimigo (força o clinch) e atira na face
        if (e.isMyFault()) {
            setBack(50);
        }
        setFire(3.0); // Tiro de dano máximo de curto alcance
        setTurnRight(e.getBearing() + 90);
        execute();
    }
    
    // --- CORREÇÃO DE TRAVAMENTO NA PAREDE (onHitWall) ---
    
    /**
     * Reverte a direção do movimento e se afasta da parede IMEDIATAMENTE.
     */
    public void onHitWall(HitWallEvent e) {
        // Inverte o sentido de movimento da variável controladora
        direcaoMovimento *= -1; 
        
        // Força o giro de desvio e o movimento para a nova direção
        setTurnRight(10); 
        setAhead(100 * direcaoMovimento);
        
        execute(); 
    }
}