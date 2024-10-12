package org.example.Script;



import org.example.POST.Attack;
import org.example.POST.Transport;
import org.example.models.move.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ShootScript {
    public List<TransportAction> shoot(List<TransportAction> transports, GameState gameState) {
        return transports.stream()
                .map(transport -> findGoal(transport, gameState)) // Обновляем каждый транспорт
                .collect(Collectors.toList());
    }

    private TransportAction findGoal(TransportAction transport, GameState gameState) {
        // Находим соответствующий Transport1 по id
        TransportResponse currentTransport = gameState.getTransports().stream()
                .filter(t1 -> t1.getId().equals(transport.getId()))
                .findFirst()
                .orElse(null);

        if (currentTransport != null) {
            // Получаем состояние игры
            Attack attack = decideAttack(gameState.getEnemies(), currentTransport, gameState.getAttackRange(), gameState.getAttackExplosionRadius());
            //TODO
            if(attack != null){
                System.out.println("Корабль - " + currentTransport.toString() + "выстрелил по " + attack.getX() + ":" + attack.getY());
            }
            transport.setAttack(attack);
        }

        return transport;
    }

    private Attack decideAttack(List<Enemy> enemies, TransportResponse currentTransport, int attackRange, int attackExplosionRadius) {
        int currentHealth = currentTransport.getHealth(); // Текущее здоровье транспорта

        // 1) Проверяем шотных врагов в радиусе атаки
        for (Enemy enemy : enemies) {
            if (enemy.getHealth() <= 30) {
                double distance = Math.sqrt(Math.pow(enemy.getX() - currentTransport.getX(), 2) + Math.pow(enemy.getY() - currentTransport.getY(), 2));
                if (distance <= attackRange + attackExplosionRadius - 10) {
                    // 1.1) Если враг без щита
                    if (enemy.getShieldLeftMs() <= 0) {
                        // Проверяем возможность задетия нескольких врагов
                        Attack newAttack = canHitMultipleEnemies(enemies, new Attack(enemy.getX(), enemy.getY()), attackExplosionRadius - 5, attackRange, enemy, currentTransport);
                        if (newAttack != null) {
                            return newAttack; // Возвращаем новые координаты
                        } else {
                            return new Attack(enemy.getX(), enemy.getY()); // Возвращаем атаку, если только один враг
                        }
                    }
                }
            }
        }

        // 1.2) Проверяем на наличие шотных врагов с щитом
        for (Enemy enemy : enemies) {
            if (enemy.getHealth() <= 30) {
                double distance = Math.sqrt(Math.pow(enemy.getX() - currentTransport.getX(), 2) + Math.pow(enemy.getY() - currentTransport.getY(), 2));
                if (distance <= attackRange + attackExplosionRadius && enemy.getShieldLeftMs() > 0) {
                    System.out.println("Транспорт " + currentTransport.toString() + "Не стрелял, так как есть шотные враги со щитом в мин дальности");
                    return null;
                }
            }
        }

        // 2) Если врагов меньше 4, выбираем ближайшего
        if (enemies.size() <= 4) {
            System.out.println("Транспорт " + currentTransport.toString() + "Не стрелял");
           return  findClosestEnemy(enemies, currentTransport.getX(), currentTransport.getY(),attackRange);
        }

        if(currentTransport.getShieldLeftMs()>0){
            System.out.println("Транспорт " + currentTransport.toString() + "Не стрелял, так как щит активен");
            return null;
        }


        //TODO Заменить на <100
        // 3) Если врагов 4 или больше, действуем в зависимости от здоровья
        if (currentHealth <= 100) {
            for (Enemy enemy : enemies) {
                    double distance = Math.sqrt(Math.pow(enemy.getX() - currentTransport.getX(), 2) + Math.pow(enemy.getY() - currentTransport.getY(), 2));
                    if (distance <= attackRange + attackExplosionRadius - 10) {
                        if (enemy.getShieldLeftMs() <= 0) {
                            Attack newAttack = canHitMultipleEnemies(enemies, new Attack(enemy.getX(), enemy.getY()), attackExplosionRadius - 5, attackRange, enemy, currentTransport);
                            if (newAttack != null) {
                                return newAttack; // Возвращаем новые координаты
                            } else {
                                return new Attack(enemy.getX(), enemy.getY()); // Возвращаем атаку, если только один враг
                            }
                        }
                    }
                }

        }
        System.out.println("Транспорт " + currentTransport.toString() + "Не стрелял, так как никого нет в радиусе действия или у всех 100 хп");

        return null; // Если ничего не подходит
    }


    private Attack canHitMultipleEnemies(List<Enemy> enemies, Attack attack, int explosionRadius, int attackRange, Enemy currentEnemy, TransportResponse currentTransport) {
        int hitCount = 0;

        // Перебираем врагов и проверяем, можем ли мы задеть их в радиусе взрыва
        for (Enemy enemy : enemies) {
            if (currentEnemy.getY() == enemy.getY() && currentEnemy.getX() == enemy.getX()) {
                continue;
            }
            double distance = Math.sqrt(Math.pow(enemy.getX() - attack.getX(), 2) + Math.pow(enemy.getY() - attack.getY(), 2));
            if (distance <= explosionRadius) {
                hitCount++;
            }
        }

        // Если мы можем задеть 2 и более врагов, возвращаем текущую позицию как объект Attack
        if (hitCount >= 2) {
            System.out.println("Задеваем 2");
            return new Attack(attack.getX(), attack.getY());
        }
        Attack lastAttack = null;
        int lastHitCount = 0;

        // Проверяем возможность смещения точки атаки для достижения большего количества врагов
        for (int offsetX =  - explosionRadius; offsetX <= explosionRadius; offsetX += explosionRadius / 5) {
            for (int offsetY = - explosionRadius; offsetY <= explosionRadius; offsetY += explosionRadius / 5) {
                int newAttackX = currentEnemy.getX() + offsetX;
                int newAttackY = currentEnemy.getY() + offsetY;
                double newDistance1 = Math.sqrt(Math.pow(currentTransport.getX() - newAttackX, 2) + Math.pow(currentTransport.getY() - newAttackY, 2));
                if (newDistance1 >= attackRange) {
                    continue;
                }
                int newHitCount = 0;
                Attack newAttack = null;
                for (Enemy innerEnemy : enemies) {
                    if (currentEnemy.getY() == innerEnemy.getY() && currentEnemy.getX() == innerEnemy.getX()) {
                        continue;
                    }
                    double newDistance = Math.sqrt(Math.pow(innerEnemy.getX() - newAttackX, 2) + Math.pow(innerEnemy.getY() - newAttackY, 2));
                    if (newDistance <= explosionRadius) {
                        newHitCount++;
                        newAttack = new Attack(newAttackX, newAttackY);
                    }

                }

                if (newHitCount > lastHitCount) {
                    lastHitCount = newHitCount;
                    lastAttack = newAttack;
                }

            }
        }
        System.out.println("Стреляем по " + ++lastHitCount);
        return lastAttack;

    }

    private Attack findClosestEnemy(List<Enemy> enemies, double shooterX, double shooterY, int rangeAttack) {
        return enemies.stream()
                .filter(enemy -> Math.sqrt(Math.pow(enemy.getX() - shooterX, 2) + Math.pow(enemy.getY() - shooterY, 2)) < rangeAttack) // Игнорируем мертвых врагов
                .min(Comparator.comparingDouble(enemy -> Math.sqrt(Math.pow(enemy.getX() - shooterX, 2) + Math.pow(enemy.getY() - shooterY, 2))))
                .map(enemy -> new Attack(enemy.getX(), enemy.getY()))
                .orElse(null);
    }



}
