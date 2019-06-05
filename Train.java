import java.io.IOException;
import java.util.HashSet;

public class Train 
{
    public static void train() throws IOException 
	{    	
    	
    	// =================================================================
        // --- construct indexUserTrain and indexItemTrain
        Data.indexUserTrain = new int[Data.num_train*(1+Data.setsizeA)];
        Data.indexItemTrain = new int[Data.num_train*(1+Data.setsizeA)];

        int idx = 0;
        for(int u=1; u<=Data.n; u++)
        {
            // --- check whether the user $u$ is in the training Data
            if (!Data.TrainData.containsKey(u))
            {
                continue;
            }

            // --- get a copy of the Data in indexUserTrain and indexItemTrain
            HashSet<Integer> ItemSet = new HashSet<Integer>();
            if (Data.TrainData.containsKey(u))
            {
                ItemSet = Data.TrainData.get(u);
            }
			
            for(int i : ItemSet)
            {
                Data.indexUserTrain[idx] = u;
                Data.indexItemTrain[idx] = i;
                idx += 1;
            }
        }
        
        
        // =================================================================
        for(int iter = 0; iter < Data.num_iterations; iter++) // start from 0, denotes additional training iterations
        {
            
        	// =================================================================
            int num_train2 = Data.num_train * (1+Data.setsizeA);
            idx = Data.num_train;
            while(idx < num_train2)
            {
                // --- u,j
                int u = (int) Math.floor(Math.random() * Data.n) + 1;
                int j = (int) Math.floor(Math.random() * Data.m) + 1;

                // ---
                if (Data.userRatingNumTrain[u]==0)
				{
                    continue;
				}

                // ---
                if(!Data.ItemSetTrainingData.contains(j))
				{
                    continue;
				}

                // ---
                String u_s = Integer.toString(u);
                String j_s = Integer.toString(j);
                String s = new String(u_s + "_" + j_s);
                if(Data.UserItemPairsTrain.contains(s))
				{
                    continue;
				}

                // ---
                Data.indexUserTrain[idx] = u;
                Data.indexItemTrain[idx] = j;

                // ---
                idx += 1;
            }
            // =================================================================
            
            
            // =================================================================
            for (int iter2 = 0; iter2 < num_train2; iter2++)
            {
                // --- randomly sample a user-item pair, Math.random(): [0.0, 1.0)
                int idx2 = (int) Math.floor(Math.random() * num_train2);
				                
                int u = Data.indexUserTrain[idx2];
                int i = Data.indexItemTrain[idx2];
                
                if(idx2<Data.num_train)
				{
                    FISMrmse(1f,u,i);
				}
                else
				{
                    FISMrmse(0f,u,i);
				}
            }
            // =================================================================
        }
    }
	
	
    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public static void FISMrmse(float rating, int u, int i)
    {
        HashSet<Integer> ItemSet = Data.TrainData.get(u);
        int ItemSetSize = ItemSet.size();

        // ===================================================
        // ----- normalization
        float normalizationFactor = (float) Math.pow(ItemSetSize-rating+0.0001f, Data.alpha);
        // rating=1 or 0

        // ===================================================
        // --- $U_{u\cdot}^{-i}$
        float[] U_u_i = new float[Data.d];
        for(int i2 : ItemSet)
        {
            if (i2 != i)
            {
                for (int f=0; f<Data.d; f++)
                {
                    U_u_i[f] += Data.W[i2][f];
                }
            }
        }
        for (int f=0; f<Data.d; f++)
        {
            U_u_i[f] = U_u_i[f] / normalizationFactor;
        }
        // ===================================================

        // -----
        float[] Vi_before = new float[Data.d];
        for(int f=0; f<Data.d; f++)
        {
            Vi_before[f] = Data.V[i][f];
        }

        // ----- $r_{ui}$
        float r_ui = Data.biasU[u] + Data.biasV[i];
        for (int f=0; f<Data.d; f++)
        {
            r_ui += U_u_i[f] * Data.V[i][f];
        }

        // ----- $e_{ui}$
        float e_ui = (rating - r_ui);
        Data.biasU[u] = Data.biasU[u] - Data.gamma * ( -e_ui + Data.beta_u * Data.biasU[u] );

        // ----- update $b_i$
        Data.biasV[i] = Data.biasV[i] - Data.gamma * ( -e_ui + Data.beta_v * Data.biasV[i] );

        // ----- update $V_{i\cdot}$
        for (int f=0; f<Data.d; f++)
        {
            Data.V[i][f] = Vi_before[f] - Data.gamma * ( -e_ui * U_u_i[f] + Data.alpha_v * Data.V[i][f] );
        }
        // ===================================================

        // ===================================================
        // ----- update $W_{i'\cdot}$
        for(int i2 : ItemSet)
        {
            if (i2 != i)
            {
                for (int f=0; f<Data.d; f++)
                {
                    Data.W[i2][f] = Data.W[i2][f] - Data.gamma * ( -e_ui * Vi_before[f] / normalizationFactor + Data.alpha_w * Data.W[i2][f] );
                }
            }
        }
        // ===================================================
    }
}
