import java.util.*;

public class TxHandler {

    private UTXOPool pool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        /* your code here */
    	this.pool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} as inputs are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        /* your code here */
    	UTXOPool utxo = new UTXOPool();
        int inputSum = 0;
        int outputSum = 0;
        
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input in = tx.getInput(i);
            UTXO trans = new UTXO(in.prevTxHash, in.outputIndex);
            Transaction.Output out = pool.getTxOutput(trans);

            if (pool.contains(trans)) {
                return false;
            }
            if(!Crypto.verifySignature(out.address, tx.getRawDataToSign(i), in.signature) || utxo.contains(trans)) {
            	return false;
            }

            utxo.addUTXO(trans, out);
            inputSum += out.value;
        }

        


        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        /* your code here */
    	HashSet<Transaction> vTrans = new HashSet<>();

        for (Transaction trans : possibleTxs) {
            if (isValidTx(trans)) {
                vTrans.add(trans);

                for (Transaction.Input input : trans.getInputs()) {
                    UTXO part = new UTXO(input.prevTxHash, input.outputIndex);
                    pool.removeUTXO(part);
                }

                for (int index = 0; index < trans.numOutputs(); index++) {
                    Transaction.Output out = trans.getOutput(index);
                    UTXO part = new UTXO(trans.getHash(), index);
                    pool.addUTXO(part, out);
                }
            }
        }

        Transaction[] validTrans = new Transaction[vTrans.size()];
        return vTrans.toArray(validTrans);
    }

}
