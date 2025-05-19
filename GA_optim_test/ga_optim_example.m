clear all
close all
clc

global t t0 tf tac tdo tdo1 X0 S0 V0 Xd X Xmod Sd S Smod Ad A Amod CO2 COmod CO F V d DOd COo DOo So T DOmod mu k k1 k2 k3 k4 Kla Kla1

load ec1807kf
load ec1807k
load ec1807ac
load ec1807gs

 Xd = ec1807kf(:, 3);
 Sd = ec1807kf(:, 2);
 A  = ec1807ac(:, 2);
DOd = ec1807gs(:, 4); %O2
CO2 = ec1807gs(:, 3);
  F = ec1807kf(:, 6);
  V = ec1807k(:, 6);
%DOd = ec1807gs(:, 2);%pO2

% for X, S, F and V
  t = ec1807kf(:, 1);
tac = ec1807ac(:, 1);
tdo = ec1807gs(:, 1);

ndx = find(ec1807kf(:, 1) >= 6.69 & ec1807kf(:, 1) <= 11.57);
 t = ec1807kf(ndx, 1);
Sd = ec1807kf(ndx, 2);
Xd = ec1807kf(ndx, 3); 
 F = ec1807kf(ndx, 6);
 V = ec1807k(ndx, 6);

t0 = t(1); 
tf = t(length(t));

% ndx1 = find(ec1807ac(:, 1) >= 6.69 & ec1807ac(:, 1)<= 11.57);
% Adop = interp1(tac, A, [t0 tf]);
% tac = tac(ndx1);
% A = A(ndx1);
% Ad = [Adop(1); A];
% tac = [t0; tac];

% ndx2 = find(ec1807gs(:, 1) >= 6.69 & ec1807gs(:, 1)<= 11.57);
% 
% O2dop = interp1(tdo, DOd, [t0 tf]); 
% tdo1 = tdo(ndx2);
% DOd = DOd(ndx2);
% DOd = [O2dop(1); DOd; O2dop(2)];
% tdo1 = [t0; tdo1; tf];
% CO2dop = interp1(tdo, CO2, [t0 tf]); 
% CO2 = CO2(ndx2);
% CO2 = [CO2dop(1); CO2; CO2dop(2)];
% COo = CO2(1);
% DOo = 21.25;%O2
% DOo = 103.5;%pO2
% DOo = 21.31;% result from GAs

So = 100;
X0 = Xd(1);
S0 = Sd(1);
V0 = V(1);
% A0 = Ad(1);
% DO0 = DOd(1);
% CO0 = CO2(1);

nd = find(ec1807kf(:, 1) >= 7.224 & ec1807kf(:, 1) <= 11.57);
Sd(nd) = 2*0.08 - ec1807kf(nd,2);

NRUNS = 2;
Results = zeros(NRUNS, 5);

%	Simple Genetic Algorithm

NIND = 100;	% Number of individuals per subpopulations
MAXGEN = 100;	% Maximal number of generations
NVAR = 3;	% No of variables
% PRECI = 10;	% Precision of binary representation
MUTR = 0.2;	% Mutation rate
XOVR = 0.8; % Crossover rate
GGAP = 0.97;	% Generation gap, how many new individuals are created

% SEL_F = 'rws';	% Name of selection function
% XOV_F = 'xovdp';	% Name of recombination function for individuals
% MUT_F = 'mut';	% Name of mutation function for individuals

% Build fielddescription matrix
% FieldDR = [rep([PRECI], [1, NVAR]); [[0.3; 0.6], [0.01; 0.02], [1; 3]]; rep([0;0;1;1], [1, NVAR])]
        
% real-valued
SEL_F = 'rws';	%   Name of selection function
XOV_F = 'recint';	%   Name of recombination fun.
MUT_F = 'mutbga';	%   Name of mutation function

mmax_bound = [0.3, 0.6];
ks_bound = [0.01, 0.02];
k1_bound = [1, 3];
FieldDR = [ mmax_bound; ks_bound; k1_bound]';

for run = 1:NRUNS

t1 = cputime;

% Create population
% Chrom = crtbp(NIND, NVAR*PRECI)

%real-valued
Chrom = crtbp(NIND, FieldDR);
% reset count variables
gen = 0;	% Counter	

% Evaluate Initial Population
% ObjV = ga_error(bs2rv(Chrom, FieldDR))'

%real-valued
ObjV = ga_error(Chrom)';


% Iterate population
while gen < MAXGEN
 
 	% Fitness assignement to whole population
	FitnV = ranking(ObjV);
            
	% Select individuals from population
	SelCh = select(SEL_F, Chrom, FitnV, GGAP);
     
	% Recombine selected individuals (crossover)
	SelCh = recombin(XOV_F, SelCh, XOVR);

	% Mutate offspring
% 	SelCh = mut(SelCh, MUTR);
	% SelCh = mutate(MUT_F, SelCh);
    SelCh = mutbga(SelCh, FieldDR, MUTR);

	% Evaluate Population
% 	ObjVSel = ga_error(bs2rv(SelCh, FieldDR))

    %real-valued
    ObjVSel = ga_error(SelCh);

	% Insert offspring in population replacing parents
	[Chrom, ObjV] = reins(Chrom, SelCh, 1, 1, ObjV, ObjVSel');


	% Increment counter	
	gen = gen+1
	[y, i] = min(ObjV);
  	Error_min = min(ObjV)
	ERROR_MIN(gen) = Error_min;
	
%     Par = bs2rv(Chrom(i, :), FieldDR)

    %real-valued
    	Par = Chrom(i, :);

	PAR(gen, :) = Par;
end

[Y, I] = min(ObjV);
% ParOpt = bs2rv(Chrom(I, :), FieldDR)
ParOpt = Chrom(I, :)

Final_time = cputime-t1
Results(run, :) = [Error_min ParOpt Final_time];

end
save('results/results.mat', 'Results')
%	End of Genetic Algorithm

mu = ParOpt(1); k = ParOpt(2); k1 = ParOpt(3);

options = simset('solver', 'ode45', 'RelTol', 1e-4, 'AbsTol', 1e-6, 'MaxStep', 1);
TIMESPAN = [t(1) t(length(t))];

[T, X, Y] = sim('model_xs',  TIMESPAN, options, []);

tmod = T;
Xmod = Y(:, 1); 
Smod = Y(:, 2);
% Amod = Y(:, 3);
% DOmod = Y(:, 4);
% COmod = Y(:, 5);

Tt1 = [t0 6.7 6.8 6.9 7 7.3 7.6 7.9 8 8.3 8.6 8.9 9 9.3 9.6 9.9 10 10.3 10.6 11.2 11.4];
Xd = interp1(t, Xd, Tt1);
Sd = interp1(t, Sd, Tt1);

% DOd = interp1(tdo1, DOd, Tt1);
% CO2 = interp1(tdo1, CO2, Tt1);

figure(1)
set(findall(gcf,'-property','FontSize'),'FontSize', 14)
plot(Tt1, Xd, 'r*'), grid, hold on, 
plot(tmod, Xmod, 'b' )
legend('exp. data', 'model data', 'Location', 'northwest')
title('Results from optimization'), xlabel('Time, [h]'), ylabel('Biomass, [g/l]')

figure(2)
set(findall(gcf,'-property','FontSize'),'FontSize', 14)
plot(Tt1, Sd, 'r*'), grid, hold on, 
plot(tmod, Smod, 'b' )
legend('exp. data', 'model data')
title('Results from optimization'), xlabel('Time, [h]'), ylabel('Substrate, [g/l]')

%figure(3), plot(tac, Ad, 'r*'), hold on, plot(tmod, Amod, 'b' )
%title('Results from optimization'), xlabel('Time, [h]'), ylabel('Acetate, [g/l]')
%figure(4), plot(Tt1, DOd, 'r*'), hold on, plot(tmod, DOmod, 'g' )
%title('Results from optimization'), xlabel('Time, [h]'), ylabel('Dissolved oxygen, [%]')
%figure(5), plot(Tt1, CO2, 'r*'), hold on, plot(tmod, COmod, 'g' )
%title('Results from optimization'), xlabel('Time, [h]'), ylabel('Carbon dioxide, [%]')

% figure(7)
% set(findall(gcf,'-property','FontSize'),'FontSize', 14)
% plot(1:gen, PAR)
% title('Paramaters Estimation through Generations'), xlabel('Generations'), ylabel('MMAX, K1, K2, KS')

figure(8)
set(findall(gcf,'-property','FontSize'),'FontSize', 14)
semilogy(1:gen, ERROR_MIN, 'b')
title('Error Minimum through Generations'), xlabel('Generations'), ylabel('Error Minimum')
hold on
