clear all
close all

global t tac tdo Xd Sd Ad F V Mmax K1 K Ks N K2 E K3 Kla DOo

load generated_data.mat

 Xd = Xout;
 Sd = Sout;
  F = Fout;
  V = Vout;
% for X, S, F and V
  t = tout;
  tfe = tfeed;
%  figure(10)
%   plot(t, Xd, 'r')
%   xlabel('Time, [h]'), ylabel('Biomass,[g/l]')
%  figure(11)
%   plot(t, Sd, 'r')
%   xlabel('Time, [h]'), ylabel('Substrate, [g/l]')

t0=t(1);
tf=t(length(t));

So = 100;
X0= Xd(1);
S0 = Sd(1);
V0 = V(1);

NRUNS = 2;
Results = zeros(NRUNS, 5);


% opt X, S
% lb = [0.2 0 0.5 1]; ub = [0.8 0.1 3 10]; x0 = [0.5 0.03 2 3]; % Moser
% lb = [0.2 1 0.5]; ub = [0.8 20 3]; x0 = [0.5 6 2]; % Tessier
% lb = [0.2 0 0 0.5]; ub = [0.8 0.1 10 3]; x0 = [0.5 0.03 1 2];   

% opt A
% Mmax = 0.5402; Ks = 0.0288; K1 = 2.0118; N=;
% lb = [0]; ub = [1]; x0 = [0.05];   

% opt O2
% Mmax = 0.5402; Ks = 0.0288; K1 = 2.0118; K2=;
% lb = [0 0 19]; ub = [1 50 22]; x0 = [0.1 20 20];   
for run = 1:NRUNS
% computational time
computational_time = 0;
tt = cputime;

% Simulated Annealing

lb = [0.2 0 0.5]; ub = [0.8 0.1 3]; x0 = [0.5 0.03 2];   % Monod

options = optimoptions('simulannealbnd','PlotFcns', {@saplotbestf,@saplotf});

options = optimoptions(options, 'InitialTemperature', 70, ...
    'TemperatureFcn',@temperatureexp, 'ReannealInterval',77, 'DisplayInterval', 100, ...
    'MaxFunctionEvaluations', 500);

[par1, fval] = simulannealbnd(@error_sa_ta, x0, lb, ub, options)


computational_time = cputime-tt;
Results(run, :) = [fval par1 computational_time];
end
 Mmax = par1(1);  Ks = par1(2); K1 = par1(3); 
% Mmax = par1(1);  Ks = par1(2); K1 = par1(3); N = par1(4);
% Mmax = par1(1);  K = par1(2); K1 = par1(3);
% K2 = par1;
% K3 = par1(1);  Kla = par1(2); DOo = par1(3);

options = simset('solver', 'ode45', 'RelTol', 1e-4, 'AbsTol', 1e-6, 'MaxStep', 1);
TIMESPAN = [t(1) t(length(t))];

 [T, X, Y] = sim('Model_XS1',  TIMESPAN, options, []); % model 1
% [T, X, Y] = sim('model_xs2',  TIMESPAN, options, []); % model 2
% [T, X, Y] = sim('model_xs3',  TIMESPAN, options, []); % model 3

% [T, X, Y] = sim('model_xsa1',  TIMESPAN, options, []); % model 1
% [T, X, Y] = sim('model_xsa2',  TIMESPAN, options, []); % model 2
% [T, X, Y] = sim('model_xsa3',  TIMESPAN, options, []); % model 3

% [T, X, Y] = sim('model_xsao1',  TIMESPAN, options, []); % model 1
% [T, X, Y] = sim('model_xsao2',  TIMESPAN, options, []); % model 2
% [T, X, Y] = sim('model_xsao3',  TIMESPAN, options, []); % model 3

tmod1 = T; Xmod1 = Y(:, 1); Smod1 = Y(:, 2); 
% Amod1 = Y(:, 3); 
% DOdmod1 = Y(:, 4);

save('results/results_sa_gen.mat', 'Results')

Tt1 = [t0 6.7 6.8 6.9 7 7.3 7.6 7.9 8 8.3 8.6 8.9 9 9.3 9.6 9.9 10 10.3 10.6 11.2 11.4];
Xd = interp1(t, Xd, Tt1);
Sd = interp1(t, Sd, Tt1);

% DOd = interp1(tdo1, DOd, Tt1);
% CO2 = interp1(tdo1, CO2, Tt1);

figure(2)
set(findall(gcf,'-property','FontSize'),'FontSize', 14)
plot(Tt1, Xd, 'r*'), grid, hold on, 
plot(tmod1, Xmod1, 'b' )
legend('exp. data', 'model data', 'Location', 'northwest')
title('Results from optimization'), xlabel('Time, [h]'), ylabel('Biomass, [g/l]')

figure(3)
set(findall(gcf,'-property','FontSize'),'FontSize', 14)
plot(Tt1, Sd, 'r*'), grid, hold on, 
plot(tmod1, Smod1, 'b' )
legend('exp. data', 'model data')
title('Results from optimization'), xlabel('Time, [h]'), ylabel('Substrate, [g/l]')

% figure(5), plot(tac, Ad, 'r*'), hold on, plot(tmod1, Amod1, 'b' ), 
% %legend('Experimental data', 'Model data - SA', 2)
% title('Fed-batch cultivation process of E. coli MC4110'), 
% xlabel('Time, [h]'), ylabel('Acetate, [g/l]')
% 
% figure(6),plot(tdo, DOd, 'r*'), hold on, plot(tmod1, DOmod1, 'b' )
% title('Fed-batch cultivation process of E. coli MC4110'), 
% xlabel('Time, [h]'), ylabel('Dissolved oxygen, [%]')

