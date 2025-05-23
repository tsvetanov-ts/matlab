%_________________________________________________________________________%
%  Whale Optimization Algorithm (WOA) source codes demo 1.0               %
%                                                                         %
%  Developed in MATLAB R2011b(7.13)                                       %
%                                                                         %
%  Author and programmer: Seyedali Mirjalili                              %
%                                                                         %
%         e-Mail: ali.mirjalili@gmail.com                                 %
%                 seyedali.mirjalili@griffithuni.edu.au                   %
%                                                                         %
%       Homepage: http://www.alimirjalili.com                             %
%                                                                         %
%   Main paper: S. Mirjalili, A. Lewis                                    %
%               The Whale Optimization Algorithm,                         %
%               Advances in Engineering Software , in press,              %
%               DOI: http://dx.doi.org/10.1016/j.advengsoft.2016.01.008   %
%                                                                         %
%_________________________________________________________________________%

% You can simply define your cost in a seperate file and load its handle to fobj 
% The initial parameters that you need are:
%__________________________________________
% fobj = @YourCostFunction
% dim = number of your variables
% Max_iteration = maximum number of generations
% SearchAgents_no = number of search agents
% lb=[lb1,lb2,...,lbn] where lbn is the lower bound of variable n
% ub=[ub1,ub2,...,ubn] where ubn is the upper bound of variable n
% If all the variables have equal lower bound you can just
% define lb and ub as two single number numbers

% To run WOA: [Best_score,Best_pos,WOA_cg_curve]=WOA(SearchAgents_no,Max_iteration,lb,ub,dim,fobj)
%__________________________________________

clear all 
clc
% 
% SearchAgents_no=30; % Number of search agents
% 
% Function_name='F1'; % Name of the test function that can be from F1 to F23 (Table 1,2,3 in the paper)
% 
% Max_iteration=500; % Maximum numbef of iterations
% 
% % Load details of the selected benchmark function
% [lb,ub,dim,fobj]=Get_Functions_details(Function_name);
% 
% [Best_score,Best_pos,WOA_cg_curve]=WOA(SearchAgents_no,Max_iteration,lb,ub,dim,fobj);
% 
% figure('Position',[269   240   660   290])
% %Draw search space
% subplot(1,2,1);
% func_plot(Function_name);
% title('Parameter space')
% xlabel('x_1');
% ylabel('x_2');
% zlabel([Function_name,'( x_1 , x_2 )'])
% 
% %Draw objective space
% subplot(1,2,2);
% semilogy(WOA_cg_curve,'Color','r')
% title('Objective space')
% xlabel('Iteration');
% ylabel('Best score obtained so far');
% 
% axis tight
% grid on
% box on
% legend('WOA')
% 
% display(['The best solution obtained by WOA is : ', num2str(Best_pos)]);
% display(['The best optimal value of the objective funciton found by WOA is : ', num2str(Best_score)]);

        
% LOOP ALL OR SELECT FUNCS
%% Parameters
SearchAgents_no = 30;    % Number of search agents
Max_iteration   = 500;   % Maximum number of iterations

%% Loop over all functions F1…F24
for funcIndex = [24] % [1 6:10]
    %----------------------------------------------------------------------
    % 1) Set up the function
    %----------------------------------------------------------------------
    Function_name = sprintf('F%d', funcIndex);
    % Load bounds, dimension, and handle to the fitness function
    [lb, ub, dim, fobj] = Get_Functions_details(Function_name);

    %----------------------------------------------------------------------
    % 2) Run WOA on this function
    %----------------------------------------------------------------------
    [Best_score, Best_pos, WOA_cg_curve] = ...
        WOA(SearchAgents_no, Max_iteration, lb, ub, dim, fobj);

    %----------------------------------------------------------------------
    % 3) Plot parameter space & convergence curve
    %----------------------------------------------------------------------
    hFig = figure('Name', Function_name, ...
                  'NumberTitle','off', ...
                  'Position', [100 100 800 300]);

    % 3a) Parameter space
    subplot(1,2,1);
    func_plot(Function_name);
    title(sprintf('%s: Search Space', Function_name));
    xlabel('x_1');
    ylabel('x_2');
    zlabel([Function_name, '(\itx_1\rm,\itx_2\rm)']);

    % 3b) Objective-space convergence
    subplot(1,2,2);
    semilogy(WOA_cg_curve, 'r-','LineWidth',1.2);
    title('Objective Space Convergence');
    xlabel('Iteration');
    ylabel('Best Score So Far');
    grid on; box on; axis tight;
    legend('WOA','Location','best');

    %----------------------------------------------------------------------
    % 4) Display results in the command window
    %----------------------------------------------------------------------
    fprintf('--- %s ---\n', Function_name);
    fprintf('Best Position: [%s]\n', sprintf(' %g', Best_pos));
    fprintf('Best Objective Value: %g\n\n', Best_score);

    drawnow;  % ensure the figure updates before moving on
end


