% Marine Predator Algorithm (MPA) Implementation

% Define problem parameters
numPredators = 50; % Number of predators
numDimensions = 2; % Number of dimensions
numGenerations = 100; % Number of generations
alpha = 0.5; % Alpha parameter
beta = 0.5; % Beta parameter
gamma = 0.5; % Gamma parameter
delta = 0.5; % Delta parameter
epsilon = 0.01; % Epsilon parameter
lowerLimit = -5; % Lower limit for each dimension
upperLimit = 5; % Upper limit for each dimension

% Initialize predator positions (random)
predators = rand(numPredators, numDimensions) * (upperLimit - lowerLimit) + lowerLimit;

% Main optimization loop
for generation = 1:numGenerations
    % Step 1: Evaluate the fitness of each predator
    fitness = evaluateFitness(predators);
    
    % Step 2: Determine the leader predator (best solution)
    [bestFitness, bestIndex] = min(fitness);
    leaderPredator = predators(bestIndex, :);
    
    % Step 3: Update the position of each predator
    for i = 1:numPredators
        if rand < alpha
            predators(i, :) = leaderPredator + beta * randn(1, numDimensions);
        else
            if rand < gamma
                randPredator = predators(randi(numPredators), :);
                predators(i, :) = predators(i, :) + delta * (randPredator - predators(i, :)) + epsilon * randn(1, numDimensions);
            else
                predators(i, :) = lowerLimit + (upperLimit - lowerLimit) * rand(1, numDimensions);
            end
        end
    end
    
    % Apply boundary conditions
    predators = max(min(predators, upperLimit), lowerLimit);
    
    % Display best fitness in current generation
    disp(['Generation ', num2str(generation), ': Best Fitness = ', num2str(bestFitness)]);
end

% Evaluate fitness function (example: sphere function)
function fitness = evaluateFitness(x)
    fitness = sum(x.^2, 2);
end
